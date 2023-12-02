# advent-of-code-2023
Advent of Code 2023 solutions in Java 21

## Goals

1. Solve all the tasks
2. Write nice code
3. Only use `java.base`
4. Have all days as a standalone runnable Java file (So, like standing in the `aoc2023` folder, you could execute the day, like: `java src/main/java/io/github/zebalu/aoc2023/Day01.java`. This also means if you want to understand how a solution works, you only need to read __1__ file.)

### Stretch goals

1. All the days separately should run under 1 sec (without JVM startup)
2. All the days together should run under 10 sec (without JVM startup)

## Where are the inputs?

Compared to previous years I have found out, the maker of AoC does not want us to share the inputs. All others are in git history, so there is no use getting rid of them, but this year I won't upload any.

### But

You can set an environment variable `AOC_SESSION_ID` to your session cookie value to run `./gradlew downloadInputs` that is going to download all _already available_ inputs.

