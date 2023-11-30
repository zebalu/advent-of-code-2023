package io.github.zebalu.aoc2023;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Day01 {

	public static void main(String[] args) {
		readLines();
	}

	private static List<String> readLines() {
		try (BufferedReader br = new BufferedReader(new FileReader(new File("day01.txt"), StandardCharsets.UTF_8))) {
			return br.lines().toList();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
