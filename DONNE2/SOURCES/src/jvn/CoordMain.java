package jvn;

public class CoordMain {
	
	public static void main(String[] args) {
		try {
			JvnRemoteCoord coordImpl = new JvnCoordImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        System.out.println("Coord ready");
		
	}

}
