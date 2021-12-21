package zx.normal.Enum.枚举加接口.工厂模式实现;

import zx.normal.Enum.枚举加接口.RoleOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: zx.normal.Enum.枚举加接口.工厂模式实现
 * @version: 1.0
 */
public class RoleFactory {
    private static Map<String, RoleOperation> roleOperationMap = new HashMap<>();

    // 在静态块中先把初始化工作全部做完
    static{
        roleOperationMap.put("ROLE_ROOT_ADMIN", new RoleRootAdmin("ROLE_ROOT_ADMIN"));
        roleOperationMap.put("ROLE_ORDER_ADMIN", new RoleOrderAdmin("ROLE_ORDER_ADMIN"));
        roleOperationMap.put("ROLE_NORMAL", new RoleNormal("ROLE_NORMAL"));
    }

    /**
     * 通过传入名字获取对应的角色
     * @param roleName
     * @return
     */
    public static RoleOperation getRO(String roleName){
        return roleOperationMap.get(roleName);
    }
}

