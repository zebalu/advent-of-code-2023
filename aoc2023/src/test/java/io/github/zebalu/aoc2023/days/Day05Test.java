package io.github.zebalu.aoc2023.days;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.zebalu.aoc2023.days.Day05.SrcRange;

class Day05Test {
    
    static class SrcRangeTest {
        
        @Test
        void lengthIsCalculatedProperely() {
            SrcRange range = new SrcRange(0, 5);
            assertEquals(5, range.length());
        }
        
        @Test 
        void innerRangeIsContained() {
            SrcRange outer = new SrcRange(2, 8);
            SrcRange inner = new SrcRange(4,6);
            assertTrue(outer.contains(inner));
        }
        
        @Test 
        void sameIsContained() {
            SrcRange rng1 = new SrcRange(3, 7);
            SrcRange rng2 = new SrcRange(3,7);
            assertTrue(rng1.contains(rng2));
        }
        
        @Test
        void startsBeforeNotContained() {
            SrcRange range = new SrcRange(3, 5);
            SrcRange startsBefore = new SrcRange(1, 4);
            assertFalse(range.contains(startsBefore));
        }
        
        @Test
        void finnishAfterIsNotContained() {
            SrcRange range = new SrcRange(3, 5);
            SrcRange finnishAfter = new SrcRange(4, 6);
            assertFalse(range.contains(finnishAfter));
        }
        
        @Test
        void distinctHasNoIntersection() {
            SrcRange a = new SrcRange(2, 5);
            SrcRange b = new SrcRange(6, 8);
            assertFalse(a.hasIntersection(b));
        }
        
        @Test
        void startsBeforeHasIntersection() {
            SrcRange range = new SrcRange(3, 5);
            SrcRange startsBefore = new SrcRange(1, 4);
            assertTrue(range.hasIntersection(startsBefore));
        }
        
        @Test
        void finnishAfterHasIntersection() {
            SrcRange range = new SrcRange(3, 5);
            SrcRange finnishAfter = new SrcRange(4, 6);
            assertTrue(range.hasIntersection(finnishAfter));
        }
        
        @Test
        void intersectDistinct() {
            SrcRange a = new SrcRange(2, 5);
            SrcRange b = new SrcRange(6, 8);
            List<SrcRange> res = SrcRange.intersect(a, b);
            assertAll(()->assertTrue(res.contains(a)), ()->assertTrue(res.contains(b)));
        }
        
        @Test
        void intersectSame() {
            SrcRange a = new SrcRange(2, 5);
            SrcRange b = new SrcRange(2, 5);
            List<SrcRange> res = SrcRange.intersect(a, b);
            assertAll(()->assertTrue(res.contains(a)), ()->assertEquals(1, res.size()));
        }
        
        @Test
        void intersectInner() {
            SrcRange outer = new SrcRange(2, 5);
            SrcRange inner = new SrcRange(3, 4);
            List<SrcRange> res = SrcRange.intersect(inner, outer);
            assertAll(() -> assertTrue(res.contains(new SrcRange(2, 3))),
                    () -> assertTrue(res.contains(new SrcRange(3, 4))),
                    () -> assertTrue(res.contains(new SrcRange(4, 5))), () -> assertEquals(3, res.size()));
        }
        
        @Test
        void intersectOuter() {
            SrcRange outer = new SrcRange(2, 5);
            SrcRange inner = new SrcRange(3, 4);
            List<SrcRange> res = SrcRange.intersect(outer, inner);
            assertAll(() -> assertTrue(res.contains(new SrcRange(2, 3))),
                    () -> assertTrue(res.contains(new SrcRange(3, 4))),
                    () -> assertTrue(res.contains(new SrcRange(4, 5))), () -> assertEquals(3, res.size()));
        }
        
        @Test
        void intersectBefore() {
            SrcRange first = new SrcRange(2, 5);
            SrcRange second = new SrcRange(3, 7);
            List<SrcRange> res = SrcRange.intersect(second, first);
            assertAll(() -> assertTrue(res.contains(new SrcRange(2, 3))),
                    () -> assertTrue(res.contains(new SrcRange(3, 5))),
                    () -> assertTrue(res.contains(new SrcRange(5, 7))), () -> assertEquals(3, res.size()));
        }
        
        @Test
        void intersectWithList() {
            SrcRange value = new SrcRange(3,7);
            List<SrcRange> list = List.of(new SrcRange(0, 2), new SrcRange(2,4), new SrcRange(6, 8));
            List<SrcRange> result = SrcRange.intersect(value, list);
            assertAll(()->assertEquals(6, result.size()),
                    ()->assertTrue(result.contains(new SrcRange(0, 2))),
                    ()->assertTrue(result.contains(new SrcRange(2, 3))),
                    ()->assertTrue(result.contains(new SrcRange(3, 4))),
                    ()->assertTrue(result.contains(new SrcRange(4, 6))),
                    ()->assertTrue(result.contains(new SrcRange(6, 7))),
                    ()->assertTrue(result.contains(new SrcRange(7, 8))));
        }
        
        @Test
        void intersectOfTwoLists() {
            List<SrcRange> listA = List.of(new SrcRange(3, 7), new SrcRange(7,11));
            List<SrcRange> listB = List.of(new SrcRange(0, 2), new SrcRange(2,4), new SrcRange(6, 8));
            List<SrcRange> result = SrcRange.intersect(listA, listB);
            assertAll(()->assertEquals(7, result.size()),
                    ()->assertTrue(result.contains(new SrcRange(0, 2))),
                    ()->assertTrue(result.contains(new SrcRange(2, 3))),
                    ()->assertTrue(result.contains(new SrcRange(3, 4))),
                    ()->assertTrue(result.contains(new SrcRange(4, 6))),
                    ()->assertTrue(result.contains(new SrcRange(6, 7))),
                    ()->assertTrue(result.contains(new SrcRange(7, 8))),
                    ()->assertTrue(result.contains(new SrcRange(8, 11))));
        }
        
        @Test
        void distinctThrowsOutSame() {
            var res = SrcRange.distinct(List.of(new SrcRange(2, 4), new SrcRange(5, 6), new SrcRange(2,4)));
            assertAll(
                    ()->assertEquals(2, res.size()),
                    ()->assertTrue(res.contains(new SrcRange(2, 4))),
                    ()->assertTrue(res.contains(new SrcRange(5,6)))
                    );
        }
        
        @Test
        void distinctCutsByIntersection() {
            var res = SrcRange.distinct(List.of(new SrcRange(2, 4), new SrcRange(3,5)));
            assertAll(
                    ()->assertEquals(3, res.size()),
                    ()->assertTrue(res.contains(new SrcRange(2, 3))),
                    ()->assertTrue(res.contains(new SrcRange(3, 4))),
                    ()->assertTrue(res.contains(new SrcRange(4, 5)))
                    );
        }
        
    }

}
