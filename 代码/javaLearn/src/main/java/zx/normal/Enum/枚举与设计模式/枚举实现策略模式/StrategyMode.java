package zx.normal.Enum.枚举与设计模式.枚举实现策略模式;

/**
 * @Description: zx.normal.Enum.枚举与设计模式.枚举实现策略模式
 * @version: 1.0
 */

/**
 * 基于策略模式的加减乘除计算器
 */
public class StrategyMode {
    public enum CalculatorEnum implements Calculator{
        ADDITION{
            @Override
            public Double execute(Double x,Double y) {
                return x + y;
            }
        },
        SUBTRACTION {
            @Override
            public Double execute(Double x,Double y) {
                return x - y;
            }
        },
        MULTIPLICATION {
            @Override
            public Double execute(Double x,Double y) {
                return x * y;
            }
        },
        DIVISION {
            @Override
            public Double execute(Double x,Double y) {
                return x / y;
            }
        };
    }

    public static void main(String[] args) {
        System.out.println(CalculatorEnum.ADDITION.execute(1.0, 2.0));
        System.out.println(CalculatorEnum.SUBTRACTION.execute(1.0, 2.0));
        System.out.println(CalculatorEnum.MULTIPLICATION.execute(1.0, 2.0));
        System.out.println(CalculatorEnum.DIVISION.execute(1.0, 2.0));
    }
}
