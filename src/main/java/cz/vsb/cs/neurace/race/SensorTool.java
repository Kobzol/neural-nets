package cz.vsb.cs.neurace.race;

public class SensorTool {

	public static final float longSensorLength = 30;
	public static final int numberOfPartsForLongSensor = 10;
	public static final float shortSensorLength = 2;
	public static final int numberOfPartsForShortSensor = 10;
	public static BlockDescription[] getLinearSteps(int partsCount, float length){
		BlockDescription[] parts = new BlockDescription[partsCount];
		for (int i = 0; i < partsCount; i++) {
			float partValue = 1 - (float) i / partsCount;
			parts[i] = new BlockDescription((float)i/partsCount*length, (float)(i+1)/partsCount*length, partValue);
		}
		return parts;
	}
	public static BlockDescription[] getLogaritmicsSteps(int partsCount, float length){
		BlockDescription[] parts = new BlockDescription[partsCount];
		float blockStartOffset = 0;
		float blockLength = 0;
		for (int i = 0; i < partsCount; i++) {
//			float partValue = 1 - (float) i / partsCount;
			float step = (10f - 1) / partsCount;
			float endScale = (float) Math.log10(10f - step * (i + 1));
			float startScale = (float) Math.log10(10f - step * (i));
			blockLength = (startScale - endScale) * length;
			parts[i] = new BlockDescription(blockStartOffset, blockStartOffset+blockLength, startScale);
			blockStartOffset += blockLength;
		}
		return parts;
	}
	
	public static class BlockDescription{
		public float start;
		public float end;
		public float value;
		public BlockDescription(float start, float end, float value) {
			super();
			this.start = start;
			this.end = end;
			this.value = value;
		}
		public float getLength(){
			return end-start;
		}
		
		
	}
	
	public static void main(String[] args){
		getLogaritmicsSteps(10, 1);
	}
}