package gwt.seca.client.util;

import gwt.g2d.client.graphics.Color;


public class SecaColor {
	
	public final static Color Black = new Color(0, 0, 0);
	public final static Color Gray = new Color(127, 127, 127);
	public final static Color White = new Color(255, 255, 255);
	public final static Color Red = new Color(255, 0, 0);
	public final static Color Green = new Color(0, 255, 0);
	public final static Color Blue = new Color(0, 0, 255);
	public final static Color TransparentBlack = new Color(0, 0, 0, .33);
	public final static Color TransparentGray = new Color(127, 127, 127, .33);
	public final static Color TransparentWhite = new Color(255, 255, 255, .33);
	public final static Color TransparentRed = new Color(255, 0, 0, .33);
	public final static Color TransparentGreen = new Color(0, 255, 0, .33);
	public final static Color TransparentBlue = new Color(0, 0, 255, .33);
	
	/**
	 * Creates a Color object based on the specified values for the HSB color model.
	 * h, s, v should be in [0, 1]
	 * http://en.wikipedia.org/wiki/HSL_and_HSV#From_HSV
	 * @param h 
	 * @param s
	 * @param v
	 * @return
	 */
	public static Color getHSVColor(float h, float s, float v) {
		if (h<0 || h>1 || s<0 || s>1 || v<0 || v>1)
			return new Color(0, 0, 0);
		float r, g, b;
//		float chroma = s*v;
//		float h0 = h*6;
//		float x = chroma*(1-(h0%2-1));
//		switch ((int) h0) {
//		case 0:
//			r = chroma; g = x; b = 0; break;
//		case 1:
//			r = x; g = chroma; b = 0; break;
//		case 2:
//			r = 0; g = chroma; b = x; break;
//		case 3:
//			r = 0; g = x; b = chroma; break;
//		case 4:
//			r = x; g = 0; b = chroma; break;
//		case 5:
//			r = chroma; g = 0; b = x; break;
//		default:
//			r = 0; g = 0; b = 0;
//		}
//		r += v-chroma; g += v-chroma; b += v-chroma;
		int i;
	    float f, p, q, t;
	     
	    if (s == 0){
	        r = g = b = v;
	        return new Color(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
	    }
	   
	    h *= 6;
	    if (h == 6)
	    	h = 0;
	    i  = (int) Math.floor(h);
	    f = h - i;
	    p = v *  (1 - s);
	    q = v * (1 - s * f);
	    t = v * (1 - s * (1 - f));
	   
	    switch (i) {
	        case 0:
	            r = v; g = t; b = p; break;
	        case 1:
	            r = q; g = v; b = p; break;
	        case 2:
	            r = p; g = v; b = t; break;
	        case 3:
	            r = p; g = q; b = v; break;
	        case 4:
	            r = t; g = p; b = v; break;
	        default:        // case 5:
	            r = v; g = p; b = q; break;
	    }
		return new Color(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
	}
	public static Color getHSBColor(float h, float s, float b) {
		return getHSVColor(h, s, b);
	}
	public static Color getColor(Color color) {
		return new Color(color.getR(), color.getG(), color.getB(), color.getAlpha());
	}
	public static Color getColor(String hexCode) {
		int start = 0;
		if (hexCode.length()==7)
			start = 1;
		else if (hexCode.length()!=6)
			return new Color(0, 0, 0);
		
		int r = Integer.parseInt(hexCode.substring(start, start+2), 16);
		int g = Integer.parseInt(hexCode.substring(start+2, start+4), 16);
		int b = Integer.parseInt(hexCode.substring(start+4, start+6), 16);
		return new Color(r, g, b);
	}
	public static Color getColor(int rgb) {
		int r = rgb/65536;
		int g = (rgb%65536)/256;
		int b = rgb%256;
		return new Color(r, g, b);
	}
	public static float getHueComponent(Color color) {
		float r = color.getR()/255f;                    
		float g = color.getG()/255f;
		float b = color.getB()/255f;
			
		float Min = Math.min(r,Math.min(g,b)); //Min. value of RGB
		float Max = Math.max(r,Math.max(g,b)); //Max. value of RGB
		float h = 0, s, v;
			
		if (Max==Min) {
			h=0;
		} else if (Max==r && g>=b) {
			h=60*(g-b)/(Max-Min)+0;
		} else if (Max==r && g<b) {
			h=60*(g-b)/(Max-Min)+360;
		} else if (Max==g) {
			h=60*(b-r)/(Max-Min)+120;
		} else if (Max==b) {
			h=60*(r-g)/(Max-Min)+240;
		} 
			
//		if (Max==0) {
//			s=0;
//		} else {
//			s=1-(Min/Max);
//		}
//		v=Max;
//		int[] hsv = new int[3];
//		hsv[0] = (int)(H);	/* range: 0...360 */
//		hsv[1] = (int)(S*100);	/* range: 0...100 */
//		hsv[2] = (int)(V*100);	/* range: 0...100 */
		return h/360f;
	}
	
	private SecaColor() {
	}
}
