package org.exoplatform.platform.gadget.services.ExoScriptingConsole;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Scanner;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public class ExoScriptingConsole{
	ScriptEngine _engine;
	StringWriter _out, _err;
	PrintWriter _stdout, _stderr;
	StringBuilder _history;
	
	public ExoScriptingConsole(String engineName) throws Exception{
		_out = new StringWriter();
		_err = new StringWriter();
		_stdout = new PrintWriter(_out);
		_stderr = new PrintWriter(_err);		

		_engine = new ScriptEngineManager().getEngineByName(engineName);
		if(_engine == null) throw new Exception("Cannot find script engine '" + engineName + "'");

		ScriptContext context =  _engine.getContext();
		context.setWriter(_stdout);
		context.setErrorWriter(_stderr);
		_history = new StringBuilder();
	}
	
	public String run(String script) throws Exception{
		_out.getBuffer().setLength(0);
		_engine.eval("if (typeof println == 'undefined') this.println = print;");
		_engine.eval(script);
		_history.append(script).append("\n");
		_stdout.flush();
		return _out.toString();
	}
	
	public void runInSystemTerminal(boolean isDebug) {
		String output = "";
		String outputType = "result";
		
		System.out.print("Type 'help' for available commands\n>> "); // NOSONAR
		
		Scanner scn = new Scanner(System.in);
		while(scn.hasNextLine()){
			output = "";
			outputType = "result";

			try {
				String script = scn.nextLine();

				if(script.isEmpty()) {
					System.out.print(">> ");   // NOSONAR
					continue;
				}

				if(script.equals("quit")) {
					return;
				}

				if(script.equals("help")){
					output = "history\tDisplay history\ndump\tDisplay session state\nrefresh\tClear session state\nquit\tEnd session";
				} else if(script.equals("dump")){
					Bindings bindings = this.getVariables();
					StringBuilder builder = new StringBuilder();
					for(Map.Entry<String, Object> entry:bindings.entrySet()){
						builder.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
					}
		        	String variables = builder.toString();
		        	if(variables.isEmpty()) variables = "<empty>";
		        	output = this.toString() + "\nVariables:\n" + variables;
				} else if(script.equals("refresh")) {
					this.getVariables().clear();
					output = "Session refreshed";
				} else if(script.equals("history")) {
					output = this.getHistory();
					if(output.isEmpty()) output = "<empty>";
				} else{
					output = this.run(script);
				}
			} catch(Exception e) {
				outputType = "error";
				output = "Runtime exception: " + e.getMessage();
				
				if(isDebug){
					e.printStackTrace();  // NOSONAR
				}
			}

			if(outputType.equals("error")){
				if(!isDebug){
				    output=new StringBuffer().append("ERROR: ").append(output).toString();
					System.out.print(output + "\n"); // NOSONAR
					System.out.print(">> "); // NOSONAR
				}
			} else{
				System.out.print(output + "\n"); // NOSONAR
				System.out.print(">> "); // NOSONAR
			}
		}
	}
			
	// Get variables
	public Bindings getVariables(){
		return _engine.getBindings(ScriptContext.ENGINE_SCOPE);
	}
	
	// Get history
	public String getHistory(){
		return _history.toString();
	}

	public String toString(){
		ScriptEngineFactory factory = _engine.getFactory();
		String info = "Scripting engine: " + factory.getEngineName() + " (v" + factory.getEngineVersion() + ")\n";
		info = new StringBuffer().append(info).append("Language version: ").append(factory.getLanguageName()).append(" ").append(factory.getLanguageVersion()).append("\n").toString();
		return info;
	}

	public static void main(String[] args) {
		try {
			ExoScriptingConsole console = new ExoScriptingConsole("ECMAScript");
			System.out.println(console.toString());  // NOSONAR
			console.getVariables().put("x", 5);
			System.out.println("x + 1 = " + console.run("println(x+1)"));    // NOSONAR
			console.runInSystemTerminal(false);
		} catch (Exception e) {
			e.printStackTrace();   // NOSONAR
		}
	}
}
