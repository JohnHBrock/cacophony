package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;

import edu.uci.ics.luci.util.HashCodeUtil;

public class CNodeReference {
	
	private static transient volatile Logger log = null;
	
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MetaCNode.class);
		}
		return log;
	}
	
	public static CNodeReference fromJSONObject(JSONObject x) {
		CNodeReference c = null;
		if(x != null){
			try {
				c = new CNodeReference();
				if(x.has("meta_cnode_guid")){
					c.setMetaCNodeGuid(x.getString("meta_cnode_guid"));
				}
				if(x.has("cnode_guid")){
					c.setCNodeGuid(x.getString("cnode_guid"));
				}
				
				Long lastHeartbeat;
				try{
					lastHeartbeat = x.getLong("last_heartbeat");
					c.setLastHeartbeat(lastHeartbeat);
				}
				catch(JSONException e){
				}
				
				if(x.has("access_routes_for_ui")){
					JSONArray ars = x.getJSONArray("access_routes_for_ui");
					Set<Pair<Long, String>> newArs = new TreeSet<Pair<Long,String>>(Collections.reverseOrder());
					for(int i = 0; i< ars.length(); i++){
						long priority = ars.getJSONObject(i).getLong("priority");
						String route = ars.getJSONObject(i).getString("route");
						Pair<Long,String> p = new Pair<Long,String>(priority,route);
						newArs.add(p);
					}
					c.setAccessRoutesForUI(newArs);
				}
				
				if(x.has("access_routes_for_api")){
					JSONArray ars = x.getJSONArray("access_routes_for_api");
					Set<Pair<Long, String>> newArs = new TreeSet<Pair<Long,String>>(Collections.reverseOrder());
					for(int i = 0; i< ars.length(); i++){
						long priority = ars.getJSONObject(i).getLong("priority");
						String route = ars.getJSONObject(i).getString("route");
						Pair<Long,String> p = new Pair<Long,String>(priority,route);
						newArs.add(p);
					}
					c.setAccessRoutesForAPI(newArs);
				}
			} catch (JSONException e) {
				getLog().error("Unable to convert JSONObject to "+CNodeReference.class.getCanonicalName()+" "+e+"\n"+x.toString());
				c = null;
			}
		}
		return c;
	}
	
	public static JSONObject toJSONObject(CNodeReference c) {
		JSONObject ret = null;
		if(c != null){
			try {
				ret = new JSONObject();
				if(c.getMetaCNodeGuid() != null){
					ret.put("meta_cnode_guid", c.getMetaCNodeGuid());
				}
				if(c.getCNodeGuid() != null){
					ret.put("cnode_guid", c.getCNodeGuid());
				}
				
				if(c.getLastHeartbeat() != null){
					ret.put("last_heartbeat", c.getLastHeartbeat());
				}
				
				if(c.getAccessRoutesForUI() != null){
					JSONArray ars = new JSONArray();
					for(Pair<Long,String> p:c.getAccessRoutesForUI()){
						JSONObject ar = new JSONObject();
						ar.put("priority",p.getFirst());
						ar.put("route",p.getSecond());
						ars.put(ar);
					}
					ret.put("access_routes_for_ui", ars);
				}
				
				if(c.getAccessRoutesForAPI() != null){
					JSONArray ars = new JSONArray();
					for(Pair<Long,String> p:c.getAccessRoutesForAPI()){
						JSONObject ar = new JSONObject();
						ar.put("priority",p.getFirst());
						ar.put("route",p.getSecond());
						ars.put(ar);
					}
					ret.put("access_routes_for_api", ars);
				}
			} catch (JSONException e) {
				getLog().error("Unable to convert "+CNodeReference.class.getCanonicalName()+" to JSONObject:"+e);
				ret = null;
			}
		}
		return ret;
	}
	
	
	String metaCNodeGuid = null;
	String cNodeGuid = null;
	Long lastHeartbeat = null; /* Not included in equality tests */
	Set<Pair<Long,String>> accessRoutesForUI = null;
	Set<Pair<Long,String>> accessRoutesForAPI = null;
	
	public String getMetaCNodeGuid() {
		return metaCNodeGuid;
	}
	public void setMetaCNodeGuid(String metaCNodeGuid) {
		this.metaCNodeGuid = metaCNodeGuid;
	}
	public String getCNodeGuid() {
		return cNodeGuid;
	}
	public void setCNodeGuid(String cNodeGuid) {
		this.cNodeGuid = cNodeGuid;
	}
	public Long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(Long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	public Set<Pair<Long, String>> getAccessRoutesForUI() {
		return accessRoutesForUI;
	}
	public void setAccessRoutesForUI(Set<Pair<Long, String>> accessRoutesUI) {
		this.accessRoutesForUI = accessRoutesUI;
	}

	public Set<Pair<Long, String>> getAccessRoutesForAPI() {
		return accessRoutesForAPI;
	}
	public void setAccessRoutesForAPI(Set<Pair<Long, String>> accessRoutesAPI) {
		this.accessRoutesForAPI = accessRoutesAPI;
	}
	
	public boolean equals(Object _that){
		if(this == _that) return true;
		if( !(_that instanceof CNodeReference) ) return false;
		
		CNodeReference that = (CNodeReference) _that;
		
		boolean ret = true;
		
		ret &= !((this.getMetaCNodeGuid() == null) ^ (that.getMetaCNodeGuid() == null));
		ret &= !((this.getCNodeGuid() == null) ^ (that.getCNodeGuid() == null));
		ret &= !((this.getAccessRoutesForUI() == null) ^ (that.getAccessRoutesForUI() == null));
		ret &= !((this.getAccessRoutesForAPI() == null) ^ (that.getAccessRoutesForAPI() == null));
		
		ret &= (this.getMetaCNodeGuid() == null) || this.getMetaCNodeGuid().equals(that.getMetaCNodeGuid());
		ret &= (this.getCNodeGuid() == null) || this.getCNodeGuid().equals(that.getCNodeGuid());
		ret &= (this.getAccessRoutesForUI() == null) || this.getAccessRoutesForUI().equals(that.getAccessRoutesForUI());
		ret &= (this.getAccessRoutesForAPI() == null) || this.getAccessRoutesForAPI().equals(that.getAccessRoutesForAPI());
		
		return ret;
	}
	
	public int hashCode(){
		int result = HashCodeUtil.SEED;

		result = HashCodeUtil.hash(result,this.getMetaCNodeGuid());
		result = HashCodeUtil.hash(result,this.getCNodeGuid());
		result = HashCodeUtil.hash(result,this.getAccessRoutesForUI());
		result = HashCodeUtil.hash(result,this.getAccessRoutesForAPI());
		
		return(result);
	}
	
	public CNodeReference() {
		this(null);
	}
	
	public CNodeReference(CNodeReference a) {
		super();
		if(a != null){
			this.setMetaCNodeGuid(a.getMetaCNodeGuid());
			this.setCNodeGuid(a.getCNodeGuid());
			this.setLastHeartbeat(a.getLastHeartbeat());
			this.setAccessRoutesForUI(a.getAccessRoutesForUI());
			this.setAccessRoutesForAPI(a.getAccessRoutesForAPI());
		}
	}


	public JSONObject toJSONObject() {
		return(CNodeReference.toJSONObject(this));
	}
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append("CNodeReference:{\n");
		if(getMetaCNodeGuid()!=null){
			b.append("\tMetaCNodeGuid:");
			b.append(getMetaCNodeGuid());
			b.append("\n");
		}
		if(getCNodeGuid()!=null){
			b.append("\tCNodeGuid    :");
			b.append(getCNodeGuid());
			b.append("\n");
		}
		if(getLastHeartbeat()!=null){
			b.append("\tLast heartbeat    :");
			b.append(getLastHeartbeat());
			b.append("\n");
		}
		if(getAccessRoutesForUI() != null){
			b.append("\tUI Routes:\n");
			for(Pair<Long, String> p:getAccessRoutesForUI()){
				b.append("\t\t");
				b.append(p.toString());
				b.append("\n");
			}
		}
		if(getAccessRoutesForAPI() != null){
			b.append("\tAPI Routes:\n");
			for(Pair<Long, String> p:getAccessRoutesForAPI()){
				b.append("\t\t");
				b.append(p.toString());
				b.append("\n");
			}
		}
		b.append("}\n");
		return b.toString();
	}

	

}
