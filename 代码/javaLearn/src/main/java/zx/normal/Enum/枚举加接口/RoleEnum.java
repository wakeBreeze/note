package zx.normal.Enum.枚举加接口;

/**
 * @Description: zx.normal.Enum.枚举加接口
 * @version: 1.0
 */

/**枚举替代if/else
 * 将不同角色的情况全部交由枚举类来做
 *
 */
public enum RoleEnum implements RoleOperation{
    // 系统管理员(有A操作权限)
    ROLE_ROOT_ADMIN {
        @Override
        public String op() {
            return"ROLE_ROOT_ADMIN:" + " has AAA permission";
        }
    },

    // 订单管理员(有B操作权限)
    ROLE_ORDER_ADMIN {
        @Override
        public String op() {
            return"ROLE_ORDER_ADMIN:" + " has BBB permission";
        }
    },

    // 普通用户(有C操作权限)
    ROLE_NORMAL {
        @Override
        public String op() {
            return"ROLE_NORMAL:" + " has CCC permission";
        }
    };
}

class JudgeRole{
    public static String judge(String roleName){
        // 一行代码搞定，之前的if/else统统灰飞烟灭
        //TODO 此处应该处理java.lang.IllegalArgumentException（传入参数不对时会触发）
        return RoleEnum.valueOf(roleName).op();
    }

    //这是一个main方法，程序的入口
    public static void main(String[] args) {
        System.out.println(judge("ROLE_ROOT_ADMIN"));
    }
}
