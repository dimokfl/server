import HW_3_6.HomeWorkClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HWC_Test {

    HomeWorkClass homeWorkClass;

    @BeforeEach
    public void init() {
        homeWorkClass = new HomeWorkClass();
    }

    private static Stream<Arguments> valueForTest1(){
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.arguments(new int[] {1}, new int[] {1,1,1,4,1}));
        data.add(Arguments.arguments(new int[] {8,3,1}, new int[] {1,4,8,3,1}));
        data.add(Arguments.arguments(new int[] {4,1}, new int[] {5,9,1,4,4,1}));
        data.add(Arguments.arguments(new int[] {6,8,2}, new int[] {1,1,1,3,4,6,8,2}));
        data.add(Arguments.arguments(new int[] {1,5,1}, new int[] {1,4,1,5,1}));
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("valueForTest1")
    void test1Pos(int[] r, int[] x){
        Assertions.assertArrayEquals(r, homeWorkClass.method1(x));
    }

    private static Stream<Arguments> valueForTest2(){
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.arguments(new int[] {1}, new int[] {1,1,1,5,1}));
        data.add(Arguments.arguments(new int[] {8,3,1}, new int[] {1,9,8,3,1}));
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("valueForTest2")
    void test1Neg(int[] r, int[] x){
        Assertions.assertThrows(RuntimeException.class, () -> homeWorkClass.method1(x));
    }

    private static Stream<Arguments> valueForTest3(){
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.arguments(new int[] {1,1,1,1,1}, false));
        data.add(Arguments.arguments(new int[] {1,4,1,1,1}, true));
        data.add(Arguments.arguments(new int[] {4,4,4,4,4}, false));
        data.add(Arguments.arguments(new int[] {1,1,1,1,4}, true));
        data.add(Arguments.arguments(new int[] {1,4,1,1,1}, true));
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("valueForTest3")
    void test2(int[] x, boolean r){
        Assertions.assertEquals(r, homeWorkClass.method2(x));
    }

}
