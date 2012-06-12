package edu.uci.ics.luci.cacophony;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.KeyIterator;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Quittable;
import com.quub.webserver.AccessControl;
import com.quub.webserver.RequestHandlerFactory;
import com.quub.webserver.RequestHandlerHelper;
import com.quub.webserver.WebServer;

import edu.uci.ics.luci.cacophony.directory.api.HandlerFavicon;
import edu.uci.ics.luci.cacophony.directory.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.directory.api.WebServerWarmUp;


public class Directory implements Quittable{
	
	private static final String KEYSPACE = "CacophonyKeyspaceV1_0";
	private static StringSerializer stringSerializer = null;
	private static LongSerializer longSerializer = null;
	
	private static transient volatile Logger log = null;
	private static Directory theOne = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(WebServer.class);
		}
		return log;
	}
	
	private Cluster cluster = null;
	private Keyspace ksp = null;
	private ThriftColumnFamilyTemplate<String, String> directoryServerTemplate = null;
	private final String DIRECTORY_SERVER_CF="directory_server";
	private ThriftColumnFamilyTemplate<String, String> cacophonyNodeTemplate = null;
	private final String CACOPHONY_NODE_CF="cacophony_server";
	public final static long FIVE_MINUTES  = 5 * 60 * 1000;
	private Timer heartbeat;
	
	private boolean shuttingDown = false;
	
	public synchronized void setQuitting(boolean quitting) {
		if(shuttingDown == false){
			if(quitting == true){
				shuttingDown = true;
			}
		}
		else{
			if(quitting == false){
				getLog().fatal("Trying to undo a shutdown! Can't do that");
			}
			else{
				getLog().fatal("Trying to shutdown twice! Don't do that");
			}
		}
	}	

	
	
	private Directory(){
		
		cluster = HFactory.getOrCreateCluster("CacophonyClusterV1_0","localhost:9160");
		
		ksp = HFactory.createKeyspace(KEYSPACE, cluster);
		if (ksp == null) {
			throw new RuntimeException("Unable to find keyspace");
		}
		
		stringSerializer = StringSerializer.get();
		if (stringSerializer == null) {
			throw new RuntimeException("Unable to get StringSerializer");
		}
		
		longSerializer = LongSerializer.get();
		if (longSerializer == null) {
			throw new RuntimeException("Unable to get LongSerializer");
		}
		
		directoryServerTemplate = new ThriftColumnFamilyTemplate<String,String>(ksp,
				DIRECTORY_SERVER_CF,
				stringSerializer,
				stringSerializer);
		
		cacophonyNodeTemplate = new ThriftColumnFamilyTemplate<String,String>(ksp,
				CACOPHONY_NODE_CF,
				stringSerializer,
				stringSerializer);
		
	}
	
	public static synchronized Directory getInstance(){
		if(theOne == null){
			theOne = new Directory();
		}
		return theOne;
	}
	
	public void startHeartbeat(){
		startHeartbeat(null,null);
	}
	
	public void startHeartbeat(Long delay,Long period){
		
		if(delay == null){
			delay = 0L;
		}
		
		if(period == null){
			period = FIVE_MINUTES;
		}
		
		if(heartbeat != null){
			heartbeat.cancel();
		}
		
		/*Set up the heartbeat to go every 5 minutes;*/
		 heartbeat = new Timer(true);
		 heartbeat.scheduleAtFixedRate(
			    new TimerTask(){
					@Override
			    	public void run(){
			    		try {
			    			InetAddress me = InetAddress.getLocalHost();
			    			String ip = me.getHostAddress();
			    			ColumnFamilyUpdater<String, String> updater = directoryServerTemplate.createUpdater(ip);
			    			updater.setLong("heartbeat", System.currentTimeMillis());
			    			directoryServerTemplate.update(updater);
			    		}catch (UnknownHostException e) {
			    			getLog().warn("Directory Server unable to issue heartbeat:"+e);
			    		}
					}
					}, delay, period);
		 
	}
	
	public Long getHeartbeat(String key){
		Long ret = null;
		
		KeyIterator<String> keyIterator = new KeyIterator<String>(ksp, DIRECTORY_SERVER_CF,stringSerializer);
		
		for(String keyI: keyIterator){
			try {
			    if(keyI.equals(key)){
			    	LongSerializer ls = longSerializer;
			    	ColumnFamilyResult<String, String> res = directoryServerTemplate.queryColumns(keyI);
			    	ret = ls.fromBytes(res.getString("heartbeat").getBytes());
			    }
			} catch (HectorException e) {
			}
		}
		return ret;
	}
	
	public Map<String, Long> getServers(){
		Map<String,Long> ret = new HashMap<String,Long>();
		
		KeyIterator<String> keyIterator = new KeyIterator<String>(ksp, DIRECTORY_SERVER_CF,stringSerializer);
		
		for(String keyI: keyIterator){
			try {
				LongSerializer ls = longSerializer;
		    	ColumnFamilyResult<String, String> res = directoryServerTemplate.queryColumns(keyI);
		    	ret.put(keyI, ls.fromBytes(res.getString("heartbeat").getBytes()));
			} catch (HectorException e) {
			}
		}
		return ret;
	}
	

	private static JSAPResult parseCommandLine(String[] args) throws JSAPException {
		JSAP jsap = new JSAP();
		JSAPResult config = null;
		Switch sw = null;
		FlaggedOption fl = null;
	        
		try{
			sw = new Switch("testing")
                      .setDefault("false") 
                      .setShortFlag('t') 
                      .setLongFlag("testing");
	        
			sw.setHelp("Run in testing configuration");
			jsap.registerParameter(sw);
			
			fl = new FlaggedOption("port")
        			  .setStringParser(JSAP.INTEGER_PARSER)
                      .setRequired(false) 
                      .setShortFlag('p') 
                      .setLongFlag("port");
	        
			fl.setHelp("Which port should I listen for REST commands on?");
			jsap.registerParameter(fl);
			
			fl = new FlaggedOption("config")
					.setStringParser(JSAP.STRING_PARSER)
					.setDefault(""+CacophonyGlobals.CONFIG_FILENAME_DEFAULT) 
					.setRequired(false) 
					.setShortFlag('c') 
					.setLongFlag("config");
  
			fl.setHelp("What is the name of the file with the configuation properties?");
			jsap.registerParameter(fl);
        
			sw = new Switch("help")
        			.setDefault("false") 
        			.setShortFlag('h') 
        			.setLongFlag("help");

			sw.setHelp("Show this help message"); 
			jsap.registerParameter(sw);
        
			config = jsap.parse(args);
		}
		catch(Exception e){
			config=null;
			getLog().error(e.toString());
		}
        
        // check whether the command line was valid, and if it wasn't,
        // display usage information and exit.
        if ((config == null) || !config.success() || config.getBoolean("help")) {
        	// print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
        	if(config != null){
        		for (Iterator<?> errs = config.getErrorMessageIterator(); errs.hasNext();) {
        			System.err.println("Error: " + errs.next());
        		}
        	}

            System.err.println();
            System.err.println("Usage: java " + Directory.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.err.println();
            throw new InvalidParameterException("Unable to parse command line");
        }

		return config;
	}
	
	public static void main(String[] args) {
		
		/*Set the thread name for error reporting */
		Thread.currentThread().setName(Directory.class.getName());
			
		/*Get command line options */
		JSAPResult clo = null;
		try {
			clo = parseCommandLine(args);
		} catch (JSAPException e) {
			throw new IllegalArgumentException(e);
		}  
		
		/* Get Globals and local properties */
		CacophonyGlobals g = CacophonyGlobals.getGlobals();
		try {
			PropertiesConfiguration config;
			config = new PropertiesConfiguration(clo.getString("config"));
			g.setConfig(config);
		} catch (ConfigurationException e1) {
			getLog().error("Problem loading configuration from:"+clo.getString("config")+"\n"+e1);
		}
		
		/* Get a DB Pool */
		QuubDBConnectionPool odbcp = null;
		if(clo.getBoolean("testing")){
			//odbcp = new QuubDBConnectionPool(getGlobals(), config.getString("DatabaseURL"),"swayrdb_test","swayrb767","283cb93dc3",null,null);
		}
		else{
			//odbcp = new QuubDBConnectionPool(getGlobals(), config.getString("DatabaseURL"),"swayrdb","swayrb767","283cb93dc3",5,1);
		}
		
		/* Create the webserver to catch rest action*/
		WebServer ws = null;
		try{
			Map<String, Class<? extends RequestHandlerHelper>> requestHandlerRegistry = new TreeMap<String, Class<? extends RequestHandlerHelper>>();
			requestHandlerRegistry.put("",HandlerVersion.class);
			requestHandlerRegistry.put("favicon.ico",HandlerFavicon.class);
			requestHandlerRegistry.put("version",HandlerVersion.class);

			RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(g,requestHandlerRegistry,true);
			AccessControl accessControl = new AccessControl(g);
			
			Integer port = getConfig(clo,g.getConfig(),"port");
			Boolean testing = getConfig(clo,g.getConfig(),"testing");
			
			ws = new WebServer(g, requestHandlerFactory, odbcp, port, false,testing,accessControl);
		} catch (RuntimeException e) {
			getLog().fatal("Couldn't start webserver:"+e.toString());
			if(ws != null){
				ws.setQuitting(true);
			}
			g.setQuitting(true);
		}
		
		/* Warm up web server */
		try {
			if(ws != null){
				ws.start();
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
		}
		
		if(ws != null){
			WebServerWarmUp.go(clo, ws, "http://localhost");
			if(ws.getQuitting()){
				g.setQuitting(true);
			}
		}
		
		/* Launch Directory Node */
		Directory directory = new Directory();
		directory.startHeartbeat();
		
		/*Set up clean shutdown hooks*/
		g.addQuittables(ws);
		g.addQuittables(directory);
		g.addQuittables(odbcp);
		
		getLog().info("\nDone in "+Directory.class.getCanonicalName()+" main()\n");
		
	}

	@SuppressWarnings("unchecked")
	private static <T> T getConfig(JSAPResult clo, PropertiesConfiguration config, String string) {
		T ret = null;
		if(config.containsKey(string)){
			ret = (T) config.getProperty(string);
		}
		T _ret = (T) clo.getObject(string);
		if(_ret != null){
			ret = _ret;
		}
		return (ret);
	}

}
