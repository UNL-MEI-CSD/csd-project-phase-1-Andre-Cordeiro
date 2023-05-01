package utils;

import java.util.LinkedList;
import java.util.List;

import pt.unl.fct.di.novasys.network.data.Host;

public class View {

    private List<Host> view;
    private Host leader;
    private int viewNumber;

    public View(List<Host> view, Host leader, int viewNumber) {
        this.view = view;
        this.leader = leader;
        this.viewNumber = viewNumber;
    }

    public View(int viewNumber) {
        this.viewNumber = viewNumber;
        this.view = new LinkedList<>();
    }

    public List<Host> getView() {
        return view;
    }

    public Host getLeader() {
        return leader;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public boolean isLeader(Host host) {
        return leader.equals(host);
    }

    public void incrementViewNumber() {
        viewNumber++;
    }

    public boolean isMember(Host host) {
        return view.contains(host);
    }

    public void addMember(Host host) {
        view.add(host);
    }

    public int size() {
        return view.size();
    }

    public void setLeader(Host leader) {
        this.leader = leader;
    }

    @Override
    public String toString() {
        return "View{" +
                "view=" + view +
                ", leader=" + leader +
                ", viewNumber=" + viewNumber +
                '}';
    }
}
