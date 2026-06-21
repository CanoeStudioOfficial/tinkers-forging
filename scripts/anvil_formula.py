#!/usr/bin/env python3
"""
Tinker's Forging anvil formula helper.

Success condition:
  target - range <= current + sum(step amounts) <= target + range
  and the last three forge steps match all recipe rules.

Example:
  python scripts/anvil_formula.py --current 0 --target 72 --range 5 --rules PUNCH_LAST HIT_SECOND_LAST UPSET_THIRD_LAST
"""

from __future__ import print_function

import argparse
import json
from collections import OrderedDict, deque


LIMIT = 150

STEPS = OrderedDict([
    ("HIT_LIGHT", -3),
    ("HIT_MEDIUM", -6),
    ("HIT_HARD", -9),
    ("DRAW", -15),
    ("PUNCH", 2),
    ("BEND", 7),
    ("UPSET", 13),
    ("SHRINK", 16),
])

ORDER_SUFFIXES = [
    ("_SECOND_LAST", "SECOND_LAST"),
    ("_THIRD_LAST", "THIRD_LAST"),
    ("_NOT_LAST", "NOT_LAST"),
    ("_LAST", "LAST"),
    ("_ANY", "ANY"),
]


def normalize_name(value):
    return value.strip().upper().replace("-", "_").replace(" ", "_")


def expand_values(values):
    expanded = []
    for value in values or []:
        for part in value.split(","):
            part = normalize_name(part)
            if part:
                expanded.append(part)
    return expanded


def parse_rule(rule_name):
    rule_name = normalize_name(rule_name)
    for suffix, order in ORDER_SUFFIXES:
        if rule_name.endswith(suffix):
            step_type = rule_name[:-len(suffix)]
            if step_type not in ("HIT", "DRAW", "PUNCH", "BEND", "UPSET", "SHRINK"):
                raise ValueError("Unknown forge rule step type: {}".format(rule_name))
            return step_type, order
    raise ValueError("Unknown forge rule order: {}".format(rule_name))


def parse_step(step_name):
    step_name = normalize_name(step_name)
    if step_name == "HIT":
        step_name = "HIT_LIGHT"
    if step_name not in STEPS:
        raise ValueError("Unknown forge step: {}".format(step_name))
    return step_name


def matches_step(rule_type, step):
    if step is None:
        return False
    if rule_type == "HIT":
        return step in ("HIT_LIGHT", "HIT_MEDIUM", "HIT_HARD")
    return step == rule_type


def matches_rule(rule, last_steps):
    rule_type, order = rule
    last = last_steps[-1] if len(last_steps) >= 1 else None
    second = last_steps[-2] if len(last_steps) >= 2 else None
    third = last_steps[-3] if len(last_steps) >= 3 else None

    if order == "ANY":
        return matches_step(rule_type, last) or matches_step(rule_type, second) or matches_step(rule_type, third)
    if order == "NOT_LAST":
        return matches_step(rule_type, second) or matches_step(rule_type, third)
    if order == "LAST":
        return matches_step(rule_type, last)
    if order == "SECOND_LAST":
        return matches_step(rule_type, second)
    if order == "THIRD_LAST":
        return matches_step(rule_type, third)
    return False


def rules_match(rules, last_steps):
    return all(matches_rule(rule, last_steps) for rule in rules)


def in_target_range(work, target, acceptable_range):
    return target - acceptable_range <= work <= target + acceptable_range


def solve(current, target, acceptable_range, rules, last_steps, worked, max_steps):
    start_steps = tuple(last_steps[-3:])
    start = (current, start_steps, worked or bool(start_steps))
    queue = deque([(start, [])])
    visited = set([start])

    while queue:
        (work, recent, has_worked), path = queue.popleft()
        if in_target_range(work, target, acceptable_range) and rules_match(rules, recent):
            return path, work, recent

        if len(path) >= max_steps:
            continue

        for step, amount in STEPS.items():
            if not has_worked and work <= 0 and amount < 0:
                continue

            next_work = work + amount
            if next_work < 0 or next_work > LIMIT:
                continue

            next_recent = (recent + (step,))[-3:]
            next_state = (next_work, next_recent, True)
            if next_state in visited:
                continue

            visited.add(next_state)
            queue.append((next_state, path + [step]))
    return None


def describe_step(step):
    amount = STEPS[step]
    return "{}({:+d})".format(step, amount)


def main():
    parser = argparse.ArgumentParser(description="Find a valid Tinker's Forging anvil step formula.")
    parser.add_argument("--current", type=int, default=0, help="Current work value. Default: 0")
    parser.add_argument("--target", type=int, required=True, help="Recipe target value shown by the red marker.")
    parser.add_argument("--range", dest="acceptable_range", type=int, default=5, help="Accepted target range. Default: 5")
    parser.add_argument("--rules", nargs="+", required=True, help="Recipe rules, for example: PUNCH_LAST HIT_SECOND_LAST UPSET_THIRD_LAST")
    parser.add_argument("--last-steps", nargs="*", default=[], help="Existing recent steps before solving, oldest to newest.")
    parser.add_argument("--worked", action="store_true", help="Allow the first generated step to be negative because the item was already worked.")
    parser.add_argument("--max-steps", type=int, default=80, help="Search limit. Default: 80")
    parser.add_argument("--json", action="store_true", help="Print machine-readable JSON.")
    args = parser.parse_args()

    if args.current < 0 or args.current > LIMIT:
        raise SystemExit("--current must be between 0 and {}".format(LIMIT))
    if args.target < 0 or args.target > LIMIT:
        raise SystemExit("--target must be between 0 and {}".format(LIMIT))
    if args.acceptable_range < 0:
        raise SystemExit("--range must be >= 0")

    rules = [parse_rule(rule) for rule in expand_values(args.rules)]
    last_steps = [parse_step(step) for step in expand_values(args.last_steps)]
    result = solve(args.current, args.target, args.acceptable_range, rules, last_steps, args.worked, args.max_steps)

    if result is None:
        raise SystemExit("No formula found within {} steps.".format(args.max_steps))

    path, final_work, final_recent = result
    total = sum(STEPS[step] for step in path)
    payload = OrderedDict([
        ("current", args.current),
        ("target", args.target),
        ("range", args.acceptable_range),
        ("accepted_min", args.target - args.acceptable_range),
        ("accepted_max", args.target + args.acceptable_range),
        ("delta", total),
        ("final_work", final_work),
        ("steps", path),
        ("step_amounts", [STEPS[step] for step in path]),
        ("last_three", list(final_recent)),
        ("rules", expand_values(args.rules)),
    ])

    if args.json:
        print(json.dumps(payload, indent=2))
        return

    print("Success formula:")
    print("  {current} + ({delta:+d}) = {final_work}".format(**payload))
    print("  accepted range: {accepted_min}..{accepted_max}".format(**payload))
    print("")
    print("Steps ({}):".format(len(path)))
    print("  " + " -> ".join(describe_step(step) for step in path))
    print("")
    print("Last three:")
    print("  " + " -> ".join(final_recent))
    print("")
    print("Rules:")
    for rule in expand_values(args.rules):
        print("  {}: {}".format(rule, "ok" if matches_rule(parse_rule(rule), final_recent) else "fail"))


if __name__ == "__main__":
    main()
