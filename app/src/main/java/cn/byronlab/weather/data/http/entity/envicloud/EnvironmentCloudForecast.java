package cn.byronlab.weather.data.http.entity.envicloud;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author byron (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/2/16
 */
public class EnvironmentCloudForecast {


    /**
     * citycode : 101020100
     * rdesc : Success
     * suggestion : {"uv":{"txt":"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。","brf":"最弱"},"cw":{"txt":"不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","brf":"不宜"},"drs":{"txt":"建议着厚外套加毛衣等服装。年老体弱者宜着大衣、呢外套加羊毛衫。","brf":"较冷"},"trav":{"txt":"温度适宜，又有较弱降水和微风作伴，会给您的旅行带来意想不到的景象，适宜旅游，可不要错过机会呦！","brf":"适宜"},"air":{"txt":"气象条件对空气污染物稀释、扩散和清除无明显影响，易感人群应适当减少室外活动时间。","brf":"中"},"comf":{"txt":"白天会有降雨，这种天气条件下，人们会感到有些凉意，但大部分人完全可以接受。","brf":"较舒适"},"sport":{"txt":"有降水，推荐您在室内进行各种健身休闲运动，若坚持户外运动，须注意保暖并携带雨具。","brf":"较不宜"},"flu":{"txt":"天气较凉，较易发生感冒，请适当增加衣服。体质较弱的朋友尤其应该注意防护。","brf":"较易发"}}
     * cityname : 上海
     * rcode : 200
     * forecast : [{"pop":"0","date":"2017-02-16","uv":"5","vis":"10","hum":"74","astro":{"ss":"17:42","mr":"22:42","ms":"09:41","sr":"06:34"},"pres":"1020","pcpn":"0.0","tmp":{"min":"10","max":"19"},"cond":{"cond_d":"小雨","cond_n":"阴"},"wind":{"sc":"微风","spd":"4","deg":"204","dir":"南风"}},{"pop":"70","date":"2017-02-17","uv":"6","vis":"10","hum":"77","astro":{"ss":"17:43","mr":"23:35","ms":"10:15","sr":"06:33"},"pres":"1025","pcpn":"0.3","tmp":{"min":"6","max":"15"},"cond":{"cond_d":"小雨","cond_n":"小雨"},"wind":{"sc":"3-4","spd":"10","deg":"28","dir":"东北风"}},{"pop":"33","date":"2017-02-18","uv":"6","vis":"10","hum":"72","astro":{"ss":"17:43","mr":"null","ms":"10:52","sr":"06:32"},"pres":"1029","pcpn":"0.0","tmp":{"min":"6","max":"10"},"cond":{"cond_d":"多云","cond_n":"晴"},"wind":{"sc":"微风","spd":"6","deg":"75","dir":"东南风"}},{"pop":"0","date":"2017-02-19","uv":"5","vis":"10","hum":"78","astro":{"ss":"17:44","mr":"00:27","ms":"11:31","sr":"06:31"},"pres":"1019","pcpn":"0.0","tmp":{"min":"10","max":"16"},"cond":{"cond_d":"多云","cond_n":"多云"},"wind":{"sc":"微风","spd":"1","deg":"174","dir":"东南风"}},{"pop":"0","date":"2017-02-20","uv":"N/A","vis":"10","hum":"81","astro":{"ss":"17:45","mr":"01:19","ms":"12:12","sr":"06:30"},"pres":"1013","pcpn":"0.0","tmp":{"min":"10","max":"19"},"cond":{"cond_d":"多云","cond_n":"小雨"},"wind":{"sc":"3-4","spd":"14","deg":"168","dir":"东北风"}},{"pop":"71","date":"2017-02-21","uv":"N/A","vis":"9","hum":"83","astro":{"ss":"17:46","mr":"02:10","ms":"12:57","sr":"06:29"},"pres":"1012","pcpn":"4.9","tmp":{"min":"7","max":"14"},"cond":{"cond_d":"小雨","cond_n":"小雨"},"wind":{"sc":"微风","spd":"0","deg":"94","dir":"东南风"}},{"pop":"100","date":"2017-02-22","uv":"N/A","vis":"2","hum":"91","astro":{"ss":"17:47","mr":"03:00","ms":"13:46","sr":"06:28"},"pres":"1016","pcpn":"9.7","tmp":{"min":"2","max":"11"},"cond":{"cond_d":"小雨","cond_n":"中雨"},"wind":{"sc":"4-5","spd":"23","deg":"340","dir":"西北风"}}]
     */


    @JSONField(name = "rcode")
    private int requestCode;//结果吗

    @JSONField(name = "rdesc")
    private String requestDesc;//结果描述

    private SuggestionEntity suggestion;//生活指数

    @JSONField(name = "citycode")
    private String cityId;//城市ID

    @JSONField(name = "cityname")
    private String cityName;//城市名

    private List<ForecastEntity> forecast;//天气预报

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public String getRequestDesc() {
        return requestDesc;
    }

    public void setRequestDesc(String requestDesc) {
        this.requestDesc = requestDesc;
    }

    public SuggestionEntity getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(SuggestionEntity suggestion) {
        this.suggestion = suggestion;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<ForecastEntity> getForecast() {
        return forecast;
    }

    public void setForecast(List<ForecastEntity> forecast) {
        this.forecast = forecast;
    }

    public static class SuggestionEntity {
        /**
         * uv : {"txt":"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。","brf":"最弱"}
         * cw : {"txt":"不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。","brf":"不宜"}
         * drs : {"txt":"建议着厚外套加毛衣等服装。年老体弱者宜着大衣、呢外套加羊毛衫。","brf":"较冷"}
         * trav : {"txt":"温度适宜，又有较弱降水和微风作伴，会给您的旅行带来意想不到的景象，适宜旅游，可不要错过机会呦！","brf":"适宜"}
         * air : {"txt":"气象条件对空气污染物稀释、扩散和清除无明显影响，易感人群应适当减少室外活动时间。","brf":"中"}
         * comf : {"txt":"白天会有降雨，这种天气条件下，人们会感到有些凉意，但大部分人完全可以接受。","brf":"较舒适"}
         * sport : {"txt":"有降水，推荐您在室内进行各种健身休闲运动，若坚持户外运动，须注意保暖并携带雨具。","brf":"较不宜"}
         * flu : {"txt":"天气较凉，较易发生感冒，请适当增加衣服。体质较弱的朋友尤其应该注意防护。","brf":"较易发"}
         */

        private UvEntity uv;//紫外线
        private CwEntity cw;//洗车指数
        private DrsEntity drs;//穿衣指数
        private TravEntity trav;//旅游指数
        private AirEntity air;//空气质量指数
        private ComfEntity comf;//s舒适度指数
        private SportEntity sport;//运动指数
        private FluEntity flu;//感冒指数

        public UvEntity getUv() {
            return uv;
        }

        public void setUv(UvEntity uv) {
            this.uv = uv;
        }

        public CwEntity getCw() {
            return cw;
        }

        public void setCw(CwEntity cw) {
            this.cw = cw;
        }

        public DrsEntity getDrs() {
            return drs;
        }

        public void setDrs(DrsEntity drs) {
            this.drs = drs;
        }

        public TravEntity getTrav() {
            return trav;
        }

        public void setTrav(TravEntity trav) {
            this.trav = trav;
        }

        public AirEntity getAir() {
            return air;
        }

        public void setAir(AirEntity air) {
            this.air = air;
        }

        public ComfEntity getComf() {
            return comf;
        }

        public void setComf(ComfEntity comf) {
            this.comf = comf;
        }

        public SportEntity getSport() {
            return sport;
        }

        public void setSport(SportEntity sport) {
            this.sport = sport;
        }

        public FluEntity getFlu() {
            return flu;
        }

        public void setFlu(FluEntity flu) {
            this.flu = flu;
        }

        public static class UvEntity {
            /**
             * txt : 属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。
             * brf : 最弱
             */

            private String txt;//生活指数详情
            private String brf;//生活指数简介

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }

        public static class CwEntity {
            /**
             * txt : 不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。
             * brf : 不宜
             */

            private String txt;
            private String brf;

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }

        public static class DrsEntity {
            /**
             * txt : 建议着厚外套加毛衣等服装。年老体弱者宜着大衣、呢外套加羊毛衫。
             * brf : 较冷
             */

            private String txt;
            private String brf;

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }

        public static class TravEntity {
            /**
             * txt : 温度适宜，又有较弱降水和微风作伴，会给您的旅行带来意想不到的景象，适宜旅游，可不要错过机会呦！
             * brf : 适宜
             */

            private String txt;
            private String brf;

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }

        public static class AirEntity {
            /**
             * txt : 气象条件对空气污染物稀释、扩散和清除无明显影响，易感人群应适当减少室外活动时间。
             * brf : 中
             */

            private String txt;
            private String brf;

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }

        public static class ComfEntity {
            /**
             * txt : 白天会有降雨，这种天气条件下，人们会感到有些凉意，但大部分人完全可以接受。
             * brf : 较舒适
             */

            private String txt;
            private String brf;

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }

        public static class SportEntity {
            /**
             * txt : 有降水，推荐您在室内进行各种健身休闲运动，若坚持户外运动，须注意保暖并携带雨具。
             * brf : 较不宜
             */

            private String txt;
            private String brf;

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }

        public static class FluEntity {
            /**
             * txt : 天气较凉，较易发生感冒，请适当增加衣服。体质较弱的朋友尤其应该注意防护。
             * brf : 较易发
             */

            private String txt;
            private String brf;

            public String getTxt() {
                return txt;
            }

            public void setTxt(String txt) {
                this.txt = txt;
            }

            public String getBrf() {
                return brf;
            }

            public void setBrf(String brf) {
                this.brf = brf;
            }
        }
    }

    public static class ForecastEntity {
        /**
         * pop : 0
         * date : 2017-02-16
         * uv : 5
         * vis : 10
         * hum : 74
         * astro : {"ss":"17:42","mr":"22:42","ms":"09:41","sr":"06:34"}
         * pres : 1020
         * pcpn : 0.0
         * tmp : {"min":"10","max":"19"}
         * cond : {"cond_d":"小雨","cond_n":"阴"}
         * wind : {"sc":"微风","spd":"4","deg":"204","dir":"南风"}
         */

        private String pop;//降水概率(%)
        private String date;//预报日期
        private String uv;//紫外线级别
        private String vis;//能见度(km)
        private String hum;//相对湿度(%)
        private AstroEntity astro;//天文数据
        private String pres;//气压(hPa)
        private String pcpn;//降水量(mm)
        private TmpEntity tmp;//气温
        private CondEntity cond;//天气现象
        private WindEntity wind;//风力风向数据

        public String getPop() {
            return pop;
        }

        public void setPop(String pop) {
            this.pop = pop;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getUv() {
            return uv;
        }

        public void setUv(String uv) {
            this.uv = uv;
        }

        public String getVis() {
            return vis;
        }

        public void setVis(String vis) {
            this.vis = vis;
        }

        public String getHum() {
            return hum;
        }

        public void setHum(String hum) {
            this.hum = hum;
        }

        public AstroEntity getAstro() {
            return astro;
        }

        public void setAstro(AstroEntity astro) {
            this.astro = astro;
        }

        public String getPres() {
            return pres;
        }

        public void setPres(String pres) {
            this.pres = pres;
        }

        public String getPcpn() {
            return pcpn;
        }

        public void setPcpn(String pcpn) {
            this.pcpn = pcpn;
        }

        public TmpEntity getTmp() {
            return tmp;
        }

        public void setTmp(TmpEntity tmp) {
            this.tmp = tmp;
        }

        public CondEntity getCond() {
            return cond;
        }

        public void setCond(CondEntity cond) {
            this.cond = cond;
        }

        public WindEntity getWind() {
            return wind;
        }

        public void setWind(WindEntity wind) {
            this.wind = wind;
        }

        public static class AstroEntity {
            /**
             * ss : 17:42
             * mr : 22:42
             * ms : 09:41
             * sr : 06:34
             */

            private String ss;//日落时间
            private String mr;//月升
            private String ms;//月落
            private String sr;//日出时间

            public String getSs() {
                return ss;
            }

            public void setSs(String ss) {
                this.ss = ss;
            }

            public String getMr() {
                return mr;
            }

            public void setMr(String mr) {
                this.mr = mr;
            }

            public String getMs() {
                return ms;
            }

            public void setMs(String ms) {
                this.ms = ms;
            }

            public String getSr() {
                return sr;
            }

            public void setSr(String sr) {
                this.sr = sr;
            }
        }

        public static class TmpEntity {
            /**
             * min : 10
             * max : 19
             */

            private String min;//最低气温(℃)
            private String max;//最高气温(℃)

            public String getMin() {
                return min;
            }

            public void setMin(String min) {
                this.min = min;
            }

            public String getMax() {
                return max;
            }

            public void setMax(String max) {
                this.max = max;
            }
        }

        public static class CondEntity {
            /**
             * cond_d : 小雨
             * cond_n : 阴
             */

            private String cond_d;//白天天气现象
            private String cond_n;//夜间天气现象

            public String getCond_d() {
                return cond_d;
            }

            public void setCond_d(String cond_d) {
                this.cond_d = cond_d;
            }

            public String getCond_n() {
                return cond_n;
            }

            public void setCond_n(String cond_n) {
                this.cond_n = cond_n;
            }
        }

        public static class WindEntity {
            /**
             * sc : 微风
             * spd : 4
             * deg : 204
             * dir : 南风
             */

            private String sc;//风力
            private String spd;//风速(m/s)
            private String deg;//风向(360°)
            private String dir;//风向

            public String getSc() {
                return sc;
            }

            public void setSc(String sc) {
                this.sc = sc;
            }

            public String getSpd() {
                return spd;
            }

            public void setSpd(String spd) {
                this.spd = spd;
            }

            public String getDeg() {
                return deg;
            }

            public void setDeg(String deg) {
                this.deg = deg;
            }

            public String getDir() {
                return dir;
            }

            public void setDir(String dir) {
                this.dir = dir;
            }
        }
    }
}
