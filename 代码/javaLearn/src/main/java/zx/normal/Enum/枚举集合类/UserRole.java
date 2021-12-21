package zx.normal.Enum.枚举集合类;

import java.util.*;

/**
 * @Description: zx.normal.Enum.枚举集合类
 * @version: 1.0
 */
public enum UserRole {

        ROLE_ROOT_ADMIN,  // 系统管理员

        ROLE_ORDER_ADMIN, // 订单管理员

        ROLE_NORMAL       // 普通用户
}
class Test{

    /**EnumSet
     * 比如系统里来了一批人，我们需要查看他是不是某个角色中的一个：
     * @param userRole
     * @return
     */
    static Boolean isAdmin(UserRole userRole){
        EnumSet<UserRole> roleRootAdmin = EnumSet.of(UserRole.ROLE_ROOT_ADMIN, UserRole.ROLE_ORDER_ADMIN);

        if (roleRootAdmin.contains(userRole)){
            return true;
        }
        return false;
    }

    /**EnumMap
     * 比如，系统里来了一批不同角色的人，
     * 我们需要统计不同的角色到底有多少人：
     * @param userRoleList
     */
    static void statisticRoleNum(List<UserRole> userRoleList){
        Map<UserRole,Integer> userRoleIntegerMap = new EnumMap<>(UserRole.class);

        for (UserRole userRole : userRoleList) {
            Integer num = userRoleIntegerMap.get(userRole);
            if (num != null){
                userRoleIntegerMap.put(userRole,num+1);
            } else {
                userRoleIntegerMap.put(userRole,1);
            }
        }

        for (UserRole role : UserRole.values()) {
            System.out.println(role.name() + ":" + userRoleIntegerMap.get(role));
        }
    }


    public static void main(String[] args) {
        System.out.println(isAdmin(UserRole.ROLE_NORMAL));
        System.out.println(isAdmin(UserRole.ROLE_ORDER_ADMIN));

        System.out.println("================================");

        List<UserRole> userRoleList = new ArrayList<>();
        userRoleList.add(UserRole.ROLE_ROOT_ADMIN);
        userRoleList.add(UserRole.ROLE_ROOT_ADMIN);
        userRoleList.add(UserRole.ROLE_ROOT_ADMIN);

        userRoleList.add(UserRole.ROLE_ORDER_ADMIN);
        userRoleList.add(UserRole.ROLE_ORDER_ADMIN);

        userRoleList.add(UserRole.ROLE_NORMAL);
        userRoleList.add(UserRole.ROLE_NORMAL);
        userRoleList.add(UserRole.ROLE_NORMAL);
        userRoleList.add(UserRole.ROLE_NORMAL);

        statisticRoleNum(userRoleList);
    }
}
