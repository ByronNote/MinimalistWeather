package cn.byronlab.weather.domain.model;

import java.util.Objects;

public final class City {

    private final String cityId;
    private final String name;
    private final String nameEn;
    private final String root;
    private final String parent;
    private final String longitude;
    private final String latitude;

    public City(String cityId, String name, String nameEn, String root, String parent,
                String longitude, String latitude) {
        this.cityId = cityId;
        this.name = name;
        this.nameEn = nameEn;
        this.root = root;
        this.parent = parent;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getCityId() {
        return cityId;
    }

    public String getName() {
        return name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getRoot() {
        return root;
    }

    public String getParent() {
        return parent;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof City)) {
            return false;
        }
        City city = (City) o;
        return Objects.equals(cityId, city.cityId)
                && Objects.equals(name, city.name)
                && Objects.equals(nameEn, city.nameEn)
                && Objects.equals(root, city.root)
                && Objects.equals(parent, city.parent)
                && Objects.equals(longitude, city.longitude)
                && Objects.equals(latitude, city.latitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, name, nameEn, root, parent, longitude, latitude);
    }

    @Override
    public String toString() {
        return "City{"
                + "cityId='" + cityId + '\''
                + ", name='" + name + '\''
                + ", nameEn='" + nameEn + '\''
                + ", root='" + root + '\''
                + ", parent='" + parent + '\''
                + ", longitude='" + longitude + '\''
                + ", latitude='" + latitude + '\''
                + '}';
    }
}
