package com.byronzhang.android.weather.data.http.entity.mi;

/**
 * 小米天气实况信息
 *
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *  {
 *     "feelsLike": {
 *         "unit": "℃",
 *         "value": "12"
 *     },
 *     "humidity": {
 *         "unit": "%",
 *         "value": "20"
 *     },
 *     "pressure": {
 *         "unit": "hPa",
 *         "value": "1010"
 *     },
 *     "pubTime": "2026-02-04T17:10:27+08:00",
 *     "temperature": {
 *         "unit": "℃",
 *         "value": "11"
 *     },
 *     "uvIndex": "0",
 *     "visibility": {
 *         "unit": "km",
 *         "value": ""
 *     },
 *     "weather": "0",
 *     "wind": {
 *         "direction": {
 *             "unit": "°",
 *             "value": "220.0"
 *         },
 *         "speed": {
 *             "unit": "km/h",
 *             "value": "6.0"
 *         }
 *     }
 * }
 */
public class MiCurrent {
    private FeelsLike feelsLike; // 体感温度
    private Humidity humidity; // 湿度
    private Pressure pressure; // 气压
    private String pubTime; // 发布时间
    private Temperature temperature; // 当前温度
    private String uvIndex; // 紫外线指数
    private Visibility visibility; // 能见度
    private String weather; // 天气代码
    private Wind wind; // 风信息

    public MiCurrent() {}

    public FeelsLike getFeelsLike() {
        return feelsLike;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public Pressure getPressure() {
        return pressure;
    }

    public String getPubTime() {
        return pubTime;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public String getUvIndex() {
        return uvIndex;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public String getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }


    public void setFeelsLike(FeelsLike feelsLike) {
        this.feelsLike = feelsLike;
    }

    public void setHumidity(Humidity humidity) {
        this.humidity = humidity;
    }

    public void setPressure(Pressure pressure) {
        this.pressure = pressure;
    }

    public void setPubTime(String pubTime) {
        this.pubTime = pubTime;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public void setUvIndex(String uvIndex) {
        this.uvIndex = uvIndex;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    /**
     * 体感温度
     */
    public static class FeelsLike {
        private String unit; // 单位
        private String value; // 数值

        public FeelsLike() {}

        public String getUnit() {
            return unit;
        }

        public String getValue() {
            return value;
        }


        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    /**
     * 湿度
     */
    public static class Humidity {
        private String unit; // 单位
        private String value; // 数值

        public Humidity() {}

        public String getUnit() {
            return unit;
        }

        public String getValue() {
            return value;
        }


        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    /**
     * 气压
     */
    public static class Pressure {
        private String unit; // 单位
        private String value; // 数值

        public Pressure() {}

        public String getUnit() {
            return unit;
        }

        public String getValue() {
            return value;
        }


        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    /**
     * 温度
     */
    public static class Temperature {
        private String unit; // 单位
        private String value; // 数值

        public Temperature() {}

        public String getUnit() {
            return unit;
        }

        public String getValue() {
            return value;
        }


        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    /**
     * 能见度
     */
    public static class Visibility {
        private String unit; // 单位
        private String value; // 数值

        public Visibility() {}

        public String getUnit() {
            return unit;
        }

        public String getValue() {
            return value;
        }


        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    /**
     * 风信息
     */
    public static class Wind {
        private Direction direction; // 风向
        private Speed speed; // 风速

        public Wind() {}

        public Direction getDirection() {
            return direction;
        }

        public Speed getSpeed() {
            return speed;
        }


        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public void setSpeed(Speed speed) {
            this.speed = speed;
        }

        /**
         * 风向
         */
        public static class Direction {
            private String unit; // 单位
            private String value; // 数值

            public Direction() {}

            public String getUnit() {
                return unit;
            }

            public String getValue() {
                return value;
            }


            public void setUnit(String unit) {
                this.unit = unit;
            }

            public void setValue(String value) {
                this.value = value;
            }

        }

        /**
         * 风速
         */
        public static class Speed {
            private String unit; // 单位
            private String value; // 数值

            public Speed() {}

            public String getUnit() {
                return unit;
            }

            public String getValue() {
                return value;
            }


            public void setUnit(String unit) {
                this.unit = unit;
            }

            public void setValue(String value) {
                this.value = value;
            }

        }
    }
}
