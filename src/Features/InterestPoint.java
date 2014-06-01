package Features;

public class InterestPoint {

	public int x;
	public int y;
	public float scale;
	public float orientation;
	public float value;
	public float[] descriptor;
	public Boolean negative;
	
	public InterestPoint(int x, int y, float scale, float value, Boolean negative) {
		// TODO Auto-generated constructor stub
		this.x = x;
		this.y = y;
		this.scale = scale;
		this.value = value;
		this.descriptor = new float[64];
		this.negative = negative;
	}

}
