package zx.normal.Enum.枚举加接口.工厂模式实现;

/**
 * @Description: zx.normal.Enum.枚举加接口.工厂模式实现
 * @version: 1.0
 */
public class JudgeRole {
    /**
     * 调用某个角色的某个方法，也可扩展其他方法
     * @param roleName
     * @return
     */
    public String judge(String roleName){// 方法控制空指针异常的处理
        if (RoleFactory.getRO(roleName) != null) {
            return RoleFactory.getRO(roleName).op();
        } else {
            return "没有" + roleName + "角色";
        }
    }

    public static void main(String[] args) {

        System.out.println(new JudgeRole().judge("ROLE_NORMALs"));
    }
}
