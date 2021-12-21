package zx.normal.Enum.demo01;

/**
 * @Description: zx.normal.Enum.demo01
 * @version: 1.0
 */
public enum VideoStatus {
    Draft,Review,Published
}

class TestEnum{
    /**增强类型约束
     * 只能传入VideoStatus类型
     * 用int类型的话，范围就是整个int，而不是指定的几个值
     * @param status
     */
    void judgeVideoStatus(VideoStatus status){

    }
}
