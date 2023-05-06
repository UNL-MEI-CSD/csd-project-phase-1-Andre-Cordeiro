package consensus.notifications;

import java.util.List;

import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;
import pt.unl.fct.di.novasys.network.data.Host;
import utils.View;

public class ViewChange extends ProtoNotification {
	
	public final static short NOTIFICATION_ID = 102;
	
	private final View view;
	
	private final int seqNumber;
	
	public ViewChange(View view, int seqNumber) {
		super(ViewChange.NOTIFICATION_ID);
		this.view = view;
		this.seqNumber = seqNumber;
	}


	public List<Host> getView() {
		return view.getView();
	}

	public int getViewNumber() {
		return view.getViewNumber();
	}

	public int getSeqNumber() {
		return seqNumber;
	}

	

}
