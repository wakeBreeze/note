package zx.normal.Enum.demo02;

/**
 * @Description: zx.normal.Enum.demo02
 * @version: 1.0
 */

/**
 * 测试枚举的基本方法
 */
public enum UserRole {
    ROLE_ROOT_ADMIN,    //系统管理员
    ROLE_ORDER_ADMIN,   //订单管理员
    ROLE_NORMAL         //普通用户
}

class Test{
    public static void main(String[] args) {
        UserRole role1 = UserRole.ROLE_ROOT_ADMIN;
        UserRole role2 = UserRole.ROLE_ORDER_ADMIN;
        UserRole role3 = UserRole.ROLE_NORMAL;

        // values()方法：返回所有枚举常量的数组集合
        for (UserRole role : UserRole.values()) {
            System.out.println(role);
        }

        // ordinal()方法：返回枚举常量的序数，注意从0开始
        System.out.println(role1.ordinal());// 打印0
        System.out.println(role2.ordinal());// 打印1
        System.out.println(role3.ordinal());// 打印2

        // compareTo()方法：枚举常量间的比较
        System.out.println( role1.compareTo(role2) ); //打印-1
        System.out.println( role2.compareTo(role3) ); //打印-1
        System.out.println( role1.compareTo(role3) ); //打印-2

        // name()方法：获得枚举常量的名称
        System.out.println(role1.name());
        System.out.println(role2.name());
        System.out.println(role3.name());

        /** valueOf()方法：返回指定名称的枚举常量
         *
         * 如果传入的不是已有枚举的名称时
         * valueOf()会抛 IllegalArgumentException
         */
        System.out.println(UserRole.valueOf("ROLE_ROOT_ADMIN"));
        System.out.println(UserRole.valueOf("ROLE_ORDER_ADMIN"));
        System.out.println(UserRole.valueOf("ROLE_NORMAL"));

        /**
         * 枚举应用于switch语句
         */
        UserRole userRole = UserRole.ROLE_ROOT_ADMIN;
        switch (userRole) {
            case ROLE_ROOT_ADMIN:
                System.out.println("这是系统管理员角色");
                break;
            case ROLE_ORDER_ADMIN:
                System.out.println("这是订单管理员角色");
                break;
            case ROLE_NORMAL:
                System.out.println("这是普通用户角色");
                break;
        }
    }
}
