package cn.byronlab.weather.data.http.entity.mi;

import java.util.List;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *
 * 小米天气指数信息（生活指数等）
 */
public class MiIndices {
    private List<Index> indices; // 指数列表
    private String pubTime; // 发布时间
    private Integer status; // 状态码（接口返回的 status 字段）

    public MiIndices() {}

    public List<Index> getIndices() {
        return indices;
    }

    public String getPubTime() {
        return pubTime;
    }

    public Integer getStatus() {
        return status;
    }


    public void setIndices(List<Index> indices) {
        this.indices = indices;
    }

    public void setPubTime(String pubTime) {
        this.pubTime = pubTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 指数条目
     */
    public static class Index {
        private String type; // 指数类型
        private String value; // 指数值

        public Index() {}

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }


        public void setType(String type) {
            this.type = type;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
