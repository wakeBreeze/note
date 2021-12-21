package zx.normal.Enum.demo03;

/**
 * @Description: zx.normal.Enum.demo03
 * @version: 1.0
 */

/**
 * 自定义扩充枚举
 *
 * 扩充UserRole枚举，在里面加入 角色名 -- 角色编码 的对应关系
 */
public enum UserRole {
    ROLE_ROOT_ADMIN("系统管理员",000000),
    ROLE_ORDER_ADMIN("订单管理员",100000),
    ROLE_NORMAL("普通用户",200000),
    ;

    //以下为自定义属性
    private final String roleName;
    private final Integer roleCode;

    // 以下为自定义构造函数
    UserRole(String roleName, Integer roleCode) {
        this.roleName = roleName;
        this.roleCode = roleCode;
    }

    // 以下为自定义方法
    public String getRoleName(){
        return this.roleName;
    }
    public Integer getRoleCode(){
        return this.roleCode;
    }
    public Integer getRoleCodeByRoleName(String roleName){
        for (UserRole role : UserRole.values()) {
            if (role.getRoleName().equals(roleName)){
                return role.getRoleCode();
            }
        }
        return null;
    }
}
class Test{
    public static void main(String[] args) {
        UserRole role = UserRole.ROLE_NORMAL;
        System.out.println(role.getRoleCodeByRoleName("普通用户"));
    }
}
