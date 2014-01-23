package edu.uci.ics.luci.cacophony.server.responder;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.PollingPolicy;
import edu.uci.ics.luci.cacophony.server.CNodeServer;
import edu.uci.ics.luci.cacophony.server.CNodeServerTest;

public class ResponderConfigurationLoaderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

//	{
//	    "request": "load_configurations",
//	    "data": {
//	        "configurations": [
//	            {
//	                "predictors": [
//	                    "p2p://edu.uci.ics.luci.cacophony.test.886800950927706967001/c_node_03"
//	                ],
//	                "c_node_name": "c_node_01",
//	                "target": {
//	                    "translator": {
//	                        "classname": "edu.uci.ics.luci.cacophony.node.TranslatorGeneric",
//	                        "options": {
//	                            "a": "thing"
//	                        }
//	                    },
//	                    "path_expression": "/*/*",
//	                    "reg_ex": "temp=(.*)",
//	                    "format": "html",
//	                    "url": "http://cnn.com"
//	                },
//	                "polling": {
//	                    "min_interval": "5000",
//	                    "policy": "on_change"
//	                }
//	            },
//	            {
//	                "predictors": [
//	                    "p2p://edu.uci.ics.luci.cacophony.test.886800950927706967001/c_node_01"
//	                ],
//	                "c_node_name": "c_node_02",
//	                "target": {
//	                    "translator": {
//	                        "classname": "edu.uci.ics.luci.cacophony.node.TranslatorGeneric",
//	                        "options": {
//	                            "b": "other thing"
//	                        }
//	                    },
//	                    "path_expression": "/*/*",
//	                    "reg_ex": "temp=(.*)",
//	                    "format": "html",
//	                    "url": "http://cnn.com"
//	                },
//	                "polling": {
//	                    "min_interval": "5000",
//	                    "policy": "on_change"
//	                }
//	            },
//	            {
//	                "predictors": [
//	                    "p2p://edu.uci.ics.luci.cacophony.test.886800950927706967001/c_node_02"
//	                ],
//	                "c_node_name": "c_node_03",
//	                "target": {
//	                    "translator": {
//	                        "classname": "edu.uci.ics.luci.cacophony.node.TranslatorGeneric",
//	                        "options": {
//	                            "c": "yet another thing"
//	                        }
//	                    },
//	                    "path_expression": "/*/*",
//	                    "reg_ex": "temp=(.*)",
//	                    "format": "html",
//	                    "url": "http://cnn.com"
//	                },
//	                "polling": {
//	                    "min_interval": "5000",
//	                    "policy": "on_change"
//	                }
//	            }
//	        ]
//	    },
//	    "from": "edu.uci.ics.luci.cacophony.test.886800950927706967001"
//	}
	public static JSONObject makeLoadConfigurationRequest(String myServerName,String from) {
		JSONObject request = new JSONObject();
		request.put("request", "load_configurations");
		request.put("from", from);
		
		JSONObject wrapper = new JSONObject();
		JSONArray configurations = new JSONArray();
		JSONObject configuration = new JSONObject();
		configuration.put("c_node_name", "c_node_01");
		JSONArray predictors = new JSONArray();
		predictors.add("p2p://"+myServerName+"/c_node_03");
		configuration.put("predictors", predictors);
		JSONObject target = new JSONObject();
		target.put("url","http://cnn.com");
		target.put("format","html");
		target.put("path_expression", "/*/*");
		target.put("reg_ex", "temp=(.*)");
		JSONObject translator = new JSONObject();
		translator.put("classname","edu.uci.ics.luci.cacophony.node.TranslatorString");
		JSONObject options = new JSONObject();
		options.put("a", "thing");
		translator.put("options",options);
		target.put("translator", translator);
		configuration.put("target", target);
		JSONObject polling = new JSONObject();
		polling.put("policy", PollingPolicy.ON_CHANGE.toString());
		polling.put("min_interval", "5000");
		configuration.put("polling", polling);
		configurations.add(configuration);
		
		configuration = new JSONObject();
		configuration.put("c_node_name", "c_node_02");
		predictors = new JSONArray();
		predictors.add("p2p://"+myServerName+"/c_node_01");
		configuration.put("predictors", predictors);
		target = new JSONObject();
		target.put("url","http://cnn.com");
		target.put("format","html");
		target.put("path_expression", "/*/*");
		target.put("reg_ex", "temp=(.*)");
		translator = new JSONObject();
		translator.put("classname","edu.uci.ics.luci.cacophony.node.TranslatorGeneric");
		options = new JSONObject();
		options.put("b", "other thing");
		translator.put("options",options);
		target.put("translator", translator);
		configuration.put("target", target);
		polling = new JSONObject();
		polling.put("policy", PollingPolicy.ON_CHANGE.toString());
		polling.put("min_interval", "5000");
		configuration.put("polling", polling);
		configurations.add(configuration);
		
		configuration = new JSONObject();
		configuration.put("c_node_name", "c_node_03");
		predictors = new JSONArray();
		predictors.add("p2p://"+myServerName+"/c_node_02");
		configuration.put("predictors", predictors);
		target = new JSONObject();
		target.put("url","http://cnn.com");
		target.put("format","html");
		target.put("path_expression", "/*/*");
		target.put("reg_ex", "temp=(.*)");
		translator = new JSONObject();
		translator.put("classname","edu.uci.ics.luci.cacophony.node.TranslatorGeneric");
		options = new JSONObject();
		options.put("c", "yet another thing");
		translator.put("options",options);
		target.put("translator", translator);
		configuration.put("target", target);
		polling = new JSONObject();
		polling.put("policy", PollingPolicy.ON_CHANGE.toString());
		polling.put("min_interval", "5000");
		configuration.put("polling", polling);
		configurations.add(configuration);
		wrapper.put("configurations", configurations);
		request.put("data", wrapper);
		
		//String jsonString = request.toJSONString(JSONStyle.LT_COMPRESS);
		return request;
	}
	

	@Test
	public void testConstructor() {
		CNodeServer cns = new CNodeServer();
		
		ResponderConfigurationLoader rcl = new ResponderConfigurationLoader(cns);
		assertTrue(rcl.getParentServer() == cns);
	}
	
	
	@Test
	public void testDegenerate() {
		ResponderConfigurationLoader rcl = null;
		
		try{
			new ResponderConfigurationLoader(null);
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			//Expected
		}
		
		
		
		String myServerName = CNodeServerTest.makeARandomP2PServerAddress();
		CNodeServer cns = new CNodeServer(myServerName,1);
		
		rcl = new ResponderConfigurationLoader(cns);
		Map<String,CNode> cNodes = new HashMap<String,CNode>();
		JSONObject jo = (JSONObject) makeLoadConfigurationRequest(myServerName, "p2p://me").get("data");
		jo.put("configurations", "Hello There!");
		
		rcl.handle(jo,cNodes);
		
		assertTrue(rcl.constructResponse().get("errors") != null);
		assertTrue(rcl.constructResponse().get("errors").toString().contains("Unable to make the \\\"configurations\\\""));
		
		
		
		
		rcl = new ResponderConfigurationLoader(cns);
		jo.put("configurations", null);
		rcl.handle(jo,cNodes);
		
		assertTrue(rcl.constructResponse().get("errors") != null);
		assertTrue(rcl.constructResponse().get("errors").toString().contains("Unable to make the \\\"configurations\\\""));
		
		
		
		
		
		rcl = new ResponderConfigurationLoader(cns);
		JSONArray breakme = new JSONArray();
		breakme.add(10);
		breakme.add(20);
		jo.put("configurations", breakme);
		rcl.handle(jo,cNodes);
		
		assertTrue(rcl.constructResponse().get("errors") != null);
		assertTrue(rcl.constructResponse().get("errors").toString().contains("Unable to make the 0th configuration"));
		
		
		
		jo = (JSONObject) makeLoadConfigurationRequest(myServerName, "p2p://me").get("data");
		rcl = new ResponderConfigurationLoader(cns);
		rcl.handle(jo,cNodes);
		rcl.handle(jo,cNodes);
		
		assertTrue(rcl.constructResponse().get("errors") != null);
		assertTrue(rcl.constructResponse().get("errors").toString().contains("Maximum number of c_nodes"));
		
	}
	
	@Test
	public void testHandleWithThree() {
		String myServerName = CNodeServerTest.makeARandomP2PServerAddress();
		CNodeServer cns = new CNodeServer(myServerName,3);
		
		ResponderConfigurationLoader rcl = new ResponderConfigurationLoader(cns);
		
		Map<String,CNode> cNodes = new HashMap<String,CNode>();
		
		rcl.handle((JSONObject) makeLoadConfigurationRequest(myServerName, "p2p://me").get("data"), cNodes);
		assertTrue(rcl.constructResponse().get("errors") == null);
		assertTrue(((JSONArray)rcl.constructResponse().get("responses")).size() > 0 );
		assertTrue(rcl.constructResponse().get("responses").toString().contains("c_node_01:OK"));
		assertTrue(rcl.constructResponse().get("responses").toString().contains("c_node_02:OK"));
		assertTrue(rcl.constructResponse().get("responses").toString().contains("c_node_03:OK"));
		
	}

}
