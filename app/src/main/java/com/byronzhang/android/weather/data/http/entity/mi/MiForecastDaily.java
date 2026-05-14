package com.byronzhang.android.weather.data.http.entity.mi;

import java.util.List;

/**
 * {
 *     "aqi": {
 *         "pubTime": "2026-02-04T00:00:00+08:00",
 *         "status": 0,
 *         "value": [
 *             78,
 *             46,
 *             36,
 *             39,
 *             39,
 *             70,
 *             66,
 *             30,
 *             48,
 *             47,
 *             63,
 *             73,
 *             30,
 *             36,
 *             60
 *         ]
 *     },
 *     "moonPhase": null,
 *     "precipitationProbability": {
 *         "status": 0,
 *         "value": [
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0",
 *             "0"
 *         ]
 *     },
 *     "pubTime": "2026-02-04T15:00:00+08:00",
 *     "status": 0,
 *     "sunRiseSet": {
 *         "status": 0,
 *         "value": [
 *             {
 *                 "from": "2026-02-04T07:21:00+08:00",
 *                 "to": "2026-02-04T17:37:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-05T07:20:00+08:00",
 *                 "to": "2026-02-05T17:38:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-06T07:19:00+08:00",
 *                 "to": "2026-02-06T17:39:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-07T07:18:00+08:00",
 *                 "to": "2026-02-07T17:40:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-08T07:16:00+08:00",
 *                 "to": "2026-02-08T17:41:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-09T07:15:00+08:00",
 *                 "to": "2026-02-09T17:42:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-10T07:14:00+08:00",
 *                 "to": "2026-02-10T17:44:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-11T07:13:00+08:00",
 *                 "to": "2026-02-11T17:45:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-12T07:12:00+08:00",
 *                 "to": "2026-02-12T17:46:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-13T07:11:00+08:00",
 *                 "to": "2026-02-13T17:47:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-14T07:09:00+08:00",
 *                 "to": "2026-02-14T17:49:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-15T07:08:00+08:00",
 *                 "to": "2026-02-15T17:50:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-16T07:06:00+08:00",
 *                 "to": "2026-02-16T17:51:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-17T07:05:00+08:00",
 *                 "to": "2026-02-17T17:53:00+08:00"
 *             },
 *             {
 *                 "from": "2026-02-18T07:04:00+08:00",
 *                 "to": "2026-02-18T17:54:00+08:00"
 *             }
 *         ]
 *     },
 *     "temperature": {
 *         "status": 0,
 *         "unit": "℃",
 *         "value": [
 *             {
 *                 "from": "12",
 *                 "to": "-1"
 *             },
 *             {
 *                 "from": "3",
 *                 "to": "-7"
 *             },
 *             {
 *                 "from": "-2",
 *                 "to": "-8"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "-9"
 *             },
 *             {
 *                 "from": "3",
 *                 "to": "-6"
 *             },
 *             {
 *                 "from": "4",
 *                 "to": "-3"
 *             },
 *             {
 *                 "from": "6",
 *                 "to": "-3"
 *             },
 *             {
 *                 "from": "8",
 *                 "to": "-2"
 *             },
 *             {
 *                 "from": "9",
 *                 "to": "-1"
 *             },
 *             {
 *                 "from": "11",
 *                 "to": "-1"
 *             },
 *             {
 *                 "from": "9",
 *                 "to": "0"
 *             },
 *             {
 *                 "from": "8",
 *                 "to": "-3"
 *             },
 *             {
 *                 "from": "9",
 *                 "to": "-2"
 *             },
 *             {
 *                 "from": "4",
 *                 "to": "-6"
 *             },
 *             {
 *                 "from": "3",
 *                 "to": "-1"
 *             }
 *         ]
 *     },
 *     "weather": {
 *         "status": 0,
 *         "value": [
 *             {
 *                 "from": "0",
 *                 "to": "0"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "1"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "1"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "0"
 *             },
 *             {
 *                 "from": "0",
 *                 "to": "0"
 *             },
 *             {
 *                 "from": "0",
 *                 "to": "1"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "0"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "0"
 *             },
 *             {
 *                 "from": "2",
 *                 "to": "1"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "2"
 *             },
 *             {
 *                 "from": "2",
 *                 "to": "2"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "0"
 *             },
 *             {
 *                 "from": "2",
 *                 "to": "2"
 *             },
 *             {
 *                 "from": "0",
 *                 "to": "2"
 *             },
 *             {
 *                 "from": "1",
 *                 "to": "0"
 *             }
 *         ]
 *     },
 *     "wind": {
 *         "direction": {
 *             "status": 0,
 *             "unit": "°",
 *             "value": [
 *                 {
 *                     "from": "220.0",
 *                     "to": "220.0"
 *                 },
 *                 {
 *                     "from": "351.42",
 *                     "to": "348.42"
 *                 },
 *                 {
 *                     "from": "297.82",
 *                     "to": "214.68"
 *                 },
 *                 {
 *                     "from": "220.6",
 *                     "to": "358.59"
 *                 },
 *                 {
 *                     "from": "2.44",
 *                     "to": "6.09"
 *                 },
 *                 {
 *                     "from": "208.78",
 *                     "to": "203.77"
 *                 },
 *                 {
 *                     "from": "196.73",
 *                     "to": "190.73"
 *                 },
 *                 {
 *                     "from": "301.08",
 *                     "to": "338.2"
 *                 },
 *                 {
 *                     "from": "11.9",
 *                     "to": "11.94"
 *                 },
 *                 {
 *                     "from": "37.71",
 *                     "to": "20.83"
 *                 },
 *                 {
 *                     "from": "310.74",
 *                     "to": "236.72"
 *                 },
 *                 {
 *                     "from": "9.62",
 *                     "to": "199.7"
 *                 },
 *                 {
 *                     "from": "355.06",
 *                     "to": "191.46"
 *                 },
 *                 {
 *                     "from": "2.48",
 *                     "to": "245.46"
 *                 },
 *                 {
 *                     "from": "354.83",
 *                     "to": "355.3"
 *                 }
 *             ]
 *         },
 *         "speed": {
 *             "status": 0,
 *             "unit": "km/h",
 *             "value": [
 *                 {
 *                     "from": "6.0",
 *                     "to": "6.0"
 *                 },
 *                 {
 *                     "from": "10.31",
 *                     "to": "16.5"
 *                 },
 *                 {
 *                     "from": "7.5",
 *                     "to": "9.81"
 *                 },
 *                 {
 *                     "from": "6.87",
 *                     "to": "11.74"
 *                 },
 *                 {
 *                     "from": "10.4",
 *                     "to": "14.59"
 *                 },
 *                 {
 *                     "from": "6.88",
 *                     "to": "8.93"
 *                 },
 *                 {
 *                     "from": "7.82",
 *                     "to": "13.34"
 *                 },
 *                 {
 *                     "from": "6.39",
 *                     "to": "8.72"
 *                 },
 *                 {
 *                     "from": "5.97",
 *                     "to": "8.35"
 *                 },
 *                 {
 *                     "from": "5.77",
 *                     "to": "7.9"
 *                 },
 *                 {
 *                     "from": "8.43",
 *                     "to": "14.04"
 *                 },
 *                 {
 *                     "from": "6.75",
 *                     "to": "9.29"
 *                 },
 *                 {
 *                     "from": "6.22",
 *                     "to": "8.15"
 *                 },
 *                 {
 *                     "from": "6.82",
 *                     "to": "9.1"
 *                 },
 *                 {
 *                     "from": "4.72",
 *                     "to": "8.34"
 *                 }
 *             ]
 *         }
 *     }
 * }
 */
public class MiForecastDaily {
    private Aqi aqi; // 空气质量指数（AQI）
    private PrecipitationProbability precipitationProbability; // 降水概率
    private String pubTime; // 发布时间
    private Integer status; // 状态码（接口返回的 status 字段）
    private SunRiseSet sunRiseSet; // 日出日落时间
    private Temperature temperature; // 温度（最高/最低）
    private Weather weather; // 天气代码（白天/夜间）
    private Wind wind; // 风信息（风向/风速）

    public MiForecastDaily() {}

    public Aqi getAqi() {
        return aqi;
    }

    public PrecipitationProbability getPrecipitationProbability() {
        return precipitationProbability;
    }

    public String getPubTime() {
        return pubTime;
    }

    public Integer getStatus() {
        return status;
    }

    public SunRiseSet getSunRiseSet() {
        return sunRiseSet;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Weather getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }


    public void setAqi(Aqi aqi) {
        this.aqi = aqi;
    }

    public void setPrecipitationProbability(PrecipitationProbability precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public void setPubTime(String pubTime) {
        this.pubTime = pubTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setSunRiseSet(SunRiseSet sunRiseSet) {
        this.sunRiseSet = sunRiseSet;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }


    public static class Aqi {
        private String pubTime; // 发布时间
        private Integer status; // 状态码（接口返回的 status 字段）
        private List<Integer> value; // AQI 数值列表（按天）

        public Aqi() {}

        public String getPubTime() {
            return pubTime;
        }

        public Integer getStatus() {
            return status;
        }

        public List<Integer> getValue() {
            return value;
        }


        public void setPubTime(String pubTime) {
            this.pubTime = pubTime;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public void setValue(List<Integer> value) {
            this.value = value;
        }
    }

    public static class PrecipitationProbability {
        private Integer status; // 状态码（接口返回的 status 字段）
        private List<String> value; // 降水概率列表（按天，字符串）

        public PrecipitationProbability() {}

        public Integer getStatus() {
            return status;
        }

        public List<String> getValue() {
            return value;
        }


        public void setStatus(Integer status) {
            this.status = status;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }

    }

    public static class SunRiseSet {
        private Integer status; // 状态码（接口返回的 status 字段）
        private List<Value> value; // 日出日落时间列表（按天）

        public SunRiseSet() {}

        public Integer getStatus() {
            return status;
        }

        public List<Value> getValue() {
            return value;
        }


        public void setStatus(Integer status) {
            this.status = status;
        }

        public void setValue(List<Value> value) {
            this.value = value;
        }


        public static class Value {
            private String from; // 日出时间（ISO 8601）
            private String to; // 日落时间（ISO 8601）

            public Value() {}

            public String getFrom() {
                return from;
            }

            public String getTo() {
                return to;
            }


            public void setFrom(String from) {
                this.from = from;
            }

            public void setTo(String to) {
                this.to = to;
            }

        }
    }

    public static class Temperature {
        private Integer status; // 状态码（接口返回的 status 字段）
        private String unit; // 温度单位（例如：℃）
        private List<Value> value; // 温度列表（按天：最高/最低）

        public Temperature() {}

        public Integer getStatus() {
            return status;
        }

        public String getUnit() {
            return unit;
        }

        public List<Value> getValue() {
            return value;
        }


        public void setStatus(Integer status) {
            this.status = status;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setValue(List<Value> value) {
            this.value = value;
        }


        public static class Value {
            private String from; // 最高温度
            private String to; // 最低温度

            public Value() {}

            public String getFrom() {
                return from;
            }

            public String getTo() {
                return to;
            }


            public void setFrom(String from) {
                this.from = from;
            }

            public void setTo(String to) {
                this.to = to;
            }

        }
    }

    public static class Weather {
        private Integer status; // 状态码（接口返回的 status 字段）
        private List<Value> value; // 天气代码列表（按天：白天/夜间）

        public Weather() {}

        public Integer getStatus() {
            return status;
        }

        public List<Value> getValue() {
            return value;
        }


        public void setStatus(Integer status) {
            this.status = status;
        }

        public void setValue(List<Value> value) {
            this.value = value;
        }


        public static class Value {
            private String from; // 白天天气代码
            private String to; // 夜间天气代码

            public Value() {}

            public String getFrom() {
                return from;
            }

            public String getTo() {
                return to;
            }


            public void setFrom(String from) {
                this.from = from;
            }

            public void setTo(String to) {
                this.to = to;
            }

        }
    }

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


        public static class Direction {
            private Integer status; // 状态码（接口返回的 status 字段）
            private String unit; // 风向单位（例如：°）
            private List<Value> value; // 风向列表（按天：白天/夜间）

            public Direction() {}

            public Integer getStatus() {
                return status;
            }

            public String getUnit() {
                return unit;
            }

            public List<Value> getValue() {
                return value;
            }


            public void setStatus(Integer status) {
                this.status = status;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public void setValue(List<Value> value) {
                this.value = value;
            }


            public static class Value {
                private String from; // 白天风向角度
                private String to; // 夜间风向角度

                public Value() {}

                public String getFrom() {
                    return from;
                }

                public String getTo() {
                    return to;
                }


                public void setFrom(String from) {
                    this.from = from;
                }

                public void setTo(String to) {
                    this.to = to;
                }

            }
        }

        public static class Speed {
            private Integer status; // 状态码（接口返回的 status 字段）
            private String unit; // 风速单位（例如：km/h）
            private List<Value> value; // 风速列表（按天：白天/夜间）

            public Speed() {}

            public Integer getStatus() {
                return status;
            }

            public String getUnit() {
                return unit;
            }

            public List<Value> getValue() {
                return value;
            }


            public void setStatus(Integer status) {
                this.status = status;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public void setValue(List<Value> value) {
                this.value = value;
            }


            public static class Value {
                private String from; // 白天风速
                private String to; // 夜间风速

                public Value() {}

                public String getFrom() {
                    return from;
                }

                public String getTo() {
                    return to;
                }


                public void setFrom(String from) {
                    this.from = from;
                }

                public void setTo(String to) {
                    this.to = to;
                }

            }
        }
    }
}
