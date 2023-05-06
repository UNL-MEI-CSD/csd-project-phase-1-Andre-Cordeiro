package utils;

import java.util.LinkedList;
import java.util.List;

import pt.unl.fct.di.novasys.network.data.Host;

public class View {

    private List<Host> view;
    private int viewNumber;

    public View(List<Host> view, int viewNumber) {
        this.view = view;
        this.viewNumber = viewNumber;
    }

    public View(int viewNumber) {
        this.viewNumber = viewNumber;
        this.view = new LinkedList<>();
    }

    public List<Host> getView() {
        return this.view;
    }

    public Host getLeader() {
        return this.view.get(this.viewNumber);
    }

    public int getViewNumber() {
        return this.viewNumber;
    }

    public boolean isLeader(Host host) {
        return this.view.get(this.viewNumber%this.view.size()).equals(host);
    }
    
    public boolean checkIfLeader(Host host, int viewNumber) {
        return this.view.get(viewNumber%this.view.size()).equals(host);
    }

    public void incrementViewNumber() {
        this.viewNumber++;
    }

    public void setViewNumber(int viewNumber) {
        this.viewNumber = viewNumber;
    }

    public boolean isMember(Host host) {
        return this.view.contains(host);
    }

    public void addMember(Host host) {
        this.view.add(host);
    }

    public int size() {
        return this.view.size();
    }

    @Override
    public String toString() {
        return "View{" +
                "view=" + this.view +
                ", viewNumber=" + this.viewNumber +
                '}';
    }
}
