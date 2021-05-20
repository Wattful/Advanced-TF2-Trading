package trading.economy;

import java.util.ArrayList;
import java.util.Collection;

class ListingList<E extends Listing> extends ArrayList<E> implements ListingCollection<E> {
	public ListingList(){
		super();
	}
	
	public ListingList(Collection<? extends E> coll){
		super(coll);
	}
}
