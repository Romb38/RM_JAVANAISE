package irc;

import jvn.MyAnotation;
public interface Operation extends Serializable{
	
	@MyAnotation(name = "read")
	String read();

	@MyAnotation(name = "write")
	void write(String s);
}