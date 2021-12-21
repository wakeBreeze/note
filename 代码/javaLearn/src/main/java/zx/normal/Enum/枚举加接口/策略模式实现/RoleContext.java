package zx.normal.Enum.枚举加接口.策略模式实现;

import zx.normal.Enum.枚举加接口.RoleOperation;

/**
 * @Description: zx.normal.Enum.枚举加接口.策略模式实现
 * @version: 1.0
 */
//策略上下文
public class RoleContext {

    // 可更换的策略，传入不同的策略对象，业务即相应变化
    private RoleOperation roleOperation;

    public RoleContext(RoleOperation roleOperation) {
        this.roleOperation = roleOperation;
    }
    public String execute(){
        return roleOperation.op();
    }
}
