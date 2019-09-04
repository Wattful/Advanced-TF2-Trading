package trading;

import java.util.LinkedHashMap;
import java.util.Map;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;

public class ListingHashSet <E extends Listing> implements Iterable<E>{
	private LinkedHashMap<String, E> set;

	public ListingHashSet(JSONArray listings, Class<E> c){
		set = new LinkedHashMap<String, E>();
		for(Object h : listings){
			try{
				h = (JSONObject)h;
			} catch (ClassCastException e){
				throw new IllegalArgumentException("Not all entries in JSON array were JSON objects.", e);
			}
			JSONObject j = (JSONObject)h;
			Class<? extends Listing> test = Listing.getType(j);
			if(!test.getName().equals(c.getName())){
				throw new IllegalArgumentException("Not all JSON objects matched the specified class.");
			}
			add(factory(j, c));
		}
	}

	public void add(E e){
		set.putIfAbsent(e.getName() + "|" + e.getEffect(), e);
	}

	public E get(Listing l){
		return get(l.getKey());
	}

	public E get(String s){
		return set.get(s);
	}

	public void remove(Listing l){
		remove(l.getKey());
	}

	public void remove(String s){
		set.remove(s);
	}

	public JSONArray getJSONRepresentation(){
		JSONArray answer = new JSONArray();
		for(Map.Entry<String, E> entry : set.entrySet()){
			answer.put(entry.getValue().getJSONRepresentation());
		}
		return answer;
	}

	public JSONArray getListingRepresentation(){
		JSONArray answer = new JSONArray();
		for(Map.Entry<String, E> entry : set.entrySet()){
			answer.put(entry.getValue().getListingRepresentation());
		}
		return answer;
	}

	public String toString(){
		return getJSONRepresentation().toString();
	}

	private E factory(JSONObject j, Class<E> c){
		E temp;
		try{
			temp = c.getConstructor(JSONObject.class).newInstance(j);
		} catch(NoSuchMethodException | InstantiationException e) {
			throw new IllegalArgumentException("Specified class must be either Hat or BuyListing.", e);
		} catch(InvocationTargetException e){
			throw new IllegalArgumentException("JSON object did not represent a " + c.getName(), e);
		} catch(IllegalAccessException e){
			throw new AssertionError("You should not see this", e);
		}
		return temp;
	}

	public Iterator<E> iterator(){
		return new MyIterator();
	}

	private class MyIterator implements Iterator<E>{
		private Iterator<Map.Entry<String, E>> itr;

		private MyIterator(){
			itr = set.entrySet().iterator();
		}

		public E next(){
			return itr.next().getValue();
		}

		public boolean hasNext(){
			return itr.hasNext();
		}
	}
}