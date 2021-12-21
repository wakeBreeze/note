package zx.normal.Enum.枚举加接口.策略模式实现;

import zx.normal.Enum.枚举加接口.RoleOperation;

/**
 * @Description: zx.normal.Enum.枚举加接口.策略模式实现
 * @version: 1.0
 */
public class JudgeRole {
    public String judge(RoleOperation roleOperation){
        RoleContext roleContext = new RoleContext(roleOperation);
        return roleContext.execute();
    }

    public static void main(String[] args) {
        JudgeRole judgeRole = new JudgeRole();
        String string1 = judgeRole.judge(new RoleRootAdmin("ROLE_ROOT_ADMIN"));
        String string2 = judgeRole.judge(new RoleOrderAdmin("ROLE_ORDER_ADMIN"));
        String string3 = judgeRole.judge(new RoleNormal("ROLE_NORMAL"));
        System.out.println(string1);
        System.out.println(string2);
        System.out.println(string3);
    }
}
