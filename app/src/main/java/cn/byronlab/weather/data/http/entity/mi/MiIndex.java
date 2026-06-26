package cn.byronlab.weather.data.http.entity.mi;

/**
 * 小米天气生活指数
 *
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         16/2/25
 */
public class MiIndex {

    private String code;
    private String details;
    private String index;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MiIndex{" +
                "code='" + code + '\'' +
                ", details='" + details + '\'' +
                ", index='" + index + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
