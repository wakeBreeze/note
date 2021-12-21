package zx.normal.Enum.枚举加接口.工厂模式实现;

import zx.normal.Enum.枚举加接口.RoleOperation;

/**
 * @Description: zx.normal.Enum.枚举加接口.工厂模式实现
 * @version: 1.0
 */
public class RoleRootAdmin implements RoleOperation {
    private String roleName;

    public RoleRootAdmin(String roleName){
        this.roleName = roleName;
    }

    @Override
    public String op() {
        return roleName + ": has AAA permission";
    }
}
