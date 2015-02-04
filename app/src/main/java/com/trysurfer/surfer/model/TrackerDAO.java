package com.trysurfer.surfer.model;

/**
 * Created by PRO on 10/9/2014.
 */
public class TrackerDAO {
    private long id, commercialId;
    private boolean opened, closed, shown;

    public TrackerDAO() {

    }

    public TrackerDAO(long id, boolean opened, boolean closed, boolean shown) {
        setId(id);
        setOpened(opened);
        setClosed(closed);
        setShown(shown);
    }

    public TrackerDAO(long id, long commercialId, boolean opened, boolean closed, boolean shown) {
        setId(id);
        setCommercialId(commercialId);
        setOpened(opened);
        setClosed(closed);
        setShown(shown);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCommercialId() {
        return commercialId;
    }

    public void setCommercialId(long CommercialId) {
        this.commercialId = CommercialId;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }
}
