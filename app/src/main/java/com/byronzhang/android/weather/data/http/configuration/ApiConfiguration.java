package com.byronzhang.android.weather.data.http.configuration;


import com.byronzhang.android.weather.data.http.ApiConstants;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2016/12/10
 */
public class ApiConfiguration {

    private int dataSourceType;

    private ApiConfiguration(Builder builder) {
        initialize(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    private void initialize(final Builder builder) {
        this.dataSourceType = builder.dataSourceType;
    }

    public int getDataSourceType() {
        return dataSourceType;
    }

    public static final class Builder {

        private int dataSourceType;

        private Builder() {
        }

        public ApiConfiguration build() {
            if (dataSourceType != ApiConstants.WEATHER_DATA_SOURCE_TYPE_MI
                    && dataSourceType != ApiConstants.WEATHER_DATA_SOURCE_TYPE_ENVIRONMENT_CLOUD) {
                throw new IllegalStateException("The dataSourceType does not support!");
            }
            return new ApiConfiguration(this);
        }

        public Builder dataSourceType(int dataSourceType) {
            this.dataSourceType = dataSourceType;
            return this;
        }
    }
}
