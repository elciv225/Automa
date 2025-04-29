package logbo.assy.automa.models;

public class ParticiperMission {
    private Mission mission;
    private Personnel personnel;

    public ParticiperMission(Mission mission, Personnel personnel) {
        this.mission = mission;
        this.personnel = personnel;
    }

    public ParticiperMission() {
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }
}
