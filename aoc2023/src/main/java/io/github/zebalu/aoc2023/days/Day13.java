package io.github.zebalu.aoc2023.days;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class Day13 {
    public static void main(String[] args) {
        String input = readInput();
        String[] patterns = input.split("\n\n");
        List<Maze> mazes = Arrays.stream(patterns).map(p->new Maze(p.lines().toList())).toList();
        System.out.println(mazes.stream().mapToInt(Maze::findAnyMirror).sum());
        System.out.println(mazes.stream().mapToInt(Maze::findAnyMirrorWith1Diff).sum());
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day13.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Maze(List<String> lines) {
        int findVerticalMirror() {
            for(int i = 0; i<lines.getFirst().length()-1; ++i) {
                boolean match = true;
                for(int y=0; y<lines.size() && match; ++y) {
                    match = checkPosinLine(i, lines.get(y));
                }
                if(match) {
                    return i+1;
                }
            }
            return -1;
        }
        
        int findHorizontalMirror() {
            for(int i = 0; i<lines.size()-1; ++i) {
                if(compareLines(i)) {
                    return i+1;
                }
            }
            return -1;
        }
        
        int findAnyMirror() {
            int i = findHorizontalMirror();
            if(i>0) {
                return i*100;
            }
            i = findVerticalMirror();
            if(i>0) {
                return i;
            }
            throw new NoSuchElementException();
        }
        
        int findVerticalMirrorWith1Diff() {
            for(int i = 0; i<lines.getFirst().length()-1; ++i) {
                int diff = 0;
                for(int y=0; y<lines.size() && diff<2; ++y) {
                    diff += checkPosinLineWith1Diff(i, lines.get(y));
                }
                if(diff == 1) {
                    return i+1;
                }
            }
            return -1;
        }
        
        int findHorizontalMirrorWith1Diff() {
            for(int i = 0; i<lines.size()-1; ++i) {
                if(compareLinesWith1Diff(i)) {
                    return (i+1);
                }
            }
            return -1;
        }
        
        int findAnyMirrorWith1Diff() {
            int i = findHorizontalMirrorWith1Diff();
            if(i>0) {
                return i*100;
            }
            i = findVerticalMirrorWith1Diff();
            if(i>0) {
                return i;
            }
            throw new NoSuchElementException();
        }
        
        private boolean checkPosinLine(int pos, String line) {
            int i = pos;
            int j = pos+1;
            boolean match = true;
            while(match && i>=0 && j < line.length()) {
                match = line.charAt(i) == line.charAt(j);
                --i;
                ++j;
            }
            return match;
        }
        
        private boolean compareLines(int lineCount) {
            int i = lineCount;
            int j = lineCount+1;
            boolean match = true;
            while( match && i>=0 && j<lines.size()) {
                for(int x=0; x<lines.get(i).length() && match; ++x) {
                    match = lines.get(i).charAt(x) == lines.get(j).charAt(x);
                }
                --i;
                ++j;
            }
            return match;
        }
        
        private int checkPosinLineWith1Diff(int pos, String line) {
            int i = pos;
            int j = pos+1;
            int diff = 0;
            while(i>=0 && j < line.length() && diff<2) {
                if(line.charAt(i) != line.charAt(j)) {
                    ++diff;
                }
                --i;
                ++j;
            }
            return diff;
        }
        
        private boolean compareLinesWith1Diff(int lineCount) {
            int i = lineCount;
            int j = lineCount+1;
            int diffCount = 0;
            while( i>=0 && j<lines.size()) {
                for(int x=0; x<lines.get(i).length() && diffCount<2; ++x) {
                    if(lines.get(i).charAt(x) != lines.get(j).charAt(x)) {
                        ++diffCount;
                    }
                }
                --i;
                ++j;
            }
            return diffCount == 1;
        }
    }
}
