package org.tub.DIMA.BDSPRO;

import org.junit.jupiter.api.Test;

public class MiscellaneousTests {
    @Test
    void testA(){

        for (int a = 0; a < 5; a++) {
            for (int b = 0; b < 5; b++) {

                int result;
                switch (a) {
                    case 1 ->
                            result = b + 100;
                    case 2 ->
                            result = b + 200;
                    case 3 ->
                            result = b + 300;
                    default ->
                            result = b + 50;
                }
                System.out.println("a:" + a + " b:" + b + ", result: " + result);
            }

        }

        int b = 10, a=5;



    }
}
