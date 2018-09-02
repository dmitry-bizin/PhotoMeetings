package com.photomeetings.model.vk;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class VKPhoto implements Serializable {

    private Integer id;// идентификатор фотографии

    @SerializedName("album_id")
    private Integer albumId;// идентификатор альбома, в котором находится фотография

    @SerializedName("owner_id")
    private Integer ownerId;// идентификатор владельца фотографии

    @SerializedName("user_id")
    private Integer userId;// идентификатор пользователя, загрузившего фото (если фотография размещена в сообществе). Для фотографий, размещенных от имени сообщества, user_id = 100

    @SerializedName("post_id")
    private Integer postId;

    private String text;// текст описания фотографии
    private Long date;// дата добавления в формате Unixtime
    private Integer width;// ширина оригинала фотографии в пикселах (на фото до 2012 года может и не быть)
    private Integer height;// высота оригинала фотографии в пикселах (на фото до 2012 года может и не быть)

    private Float lat;

    @SerializedName("long")
    private Float lng;

    private List<Size> sizes;// массив с копиями изображения в разных размера

    private transient String address;

    public VKPhoto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public List<Size> getSizes() {
        return sizes;
    }

    public void setSizes(List<Size> sizes) {
        this.sizes = sizes;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLng() {
        return lng;
    }

    public void setLng(Float lng) {
        this.lng = lng;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    @Nullable
    public String getPhotoForDetails() {
        if (sizes != null && !sizes.isEmpty()) {
            Collections.sort(sizes, new SizeComparator());
            if (sizes.size() > 1) {
                return sizes.get(sizes.size() - 2).getUrl();
            } else {
                return sizes.get(sizes.size() - 1).getUrl();
            }
        } else {
            return null;
        }
    }

    @Nullable
    public String getPhotoForMiniature() {
        if (sizes != null && !sizes.isEmpty()) {
            Collections.sort(sizes, new SizeComparator());
            if (sizes.size() > 1) {
                return sizes.get(1).getUrl();
            } else {
                return sizes.get(0).getUrl();
            }
        } else {
            return null;
        }
    }

    public boolean isNullLatLng() {
        return lat == null || lng == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VKPhoto vkPhoto = (VKPhoto) o;
        return id.equals(vkPhoto.id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
