package org.euan.equake;

import java.io.Serializable;

//Euan McDonald
//StudentID - s1927457

//Set up the EQuakeEntry Object
public class EQuakeEntry implements Serializable {

    //Default EQuakeEntry constructor
    EQuakeEntry(){
        title = "";
        description = "";
        link = "";
        pubDate = "";
        category = "";
        extra = "";
        latitude = 0.0;
        longitude = 0.0;
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) {this.description = description;}
    public void setLink(String link) {this.link = link;}
    public void setPubDate(String pubDate) {this.pubDate = pubDate;}
    public void setCategory(String category) {this.category = category;}
    public void setExtra(String extra) {this.extra = extra;}
    public void setLatitude(Double latitude) {this.latitude = latitude;}
    public void setLongitude(Double longitude) {this.longitude = longitude;}

    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public String getLink() {return link;}
    public String getPubDate() {return pubDate;}
    public String getCategory() {return category;}
    public String getExtra() {return extra;}
    public Double getLatitude() {return latitude;}
    public Double getLongitude() {return longitude;}

    String title;
    String description;
    String link;
    String pubDate;
    String category;
    String extra;
    Double latitude;
    Double longitude;
}
