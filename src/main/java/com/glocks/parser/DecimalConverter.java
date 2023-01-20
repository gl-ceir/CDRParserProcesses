package com.glocks.parser;

public class DecimalConverter {
	final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    String hex      = null;
	    try{
	    	for ( int j = 0; j < bytes.length; j++ ) {
	        	int v = bytes[j] & 0xFF;
	        	hexChars[j * 2] = hexArray[v >>> 4];
	        	hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    	}
	    	hex = new String(hexChars);
	    	// // System.out.println(hex);
	    }catch( Exception e ){
	    	e.printStackTrace();
	    }
	    return hex;
	}
	
	/*public String getStringFromByte( byte[] bytes ){
		String result = null;
		try{
			//CharBuffer cb = ByteBuffer.wrap(bytes).asCharBuffer();
			//result = cb.toString();
			result = new String(bytes, 0, bytes.length, "ASCII");
		}catch( Exception e ){
			e.printStackTrace();
			result = "0";
		}
		return result;
	}*/
	
	public String getStringFromByte( byte[] bytes ){
		String result = null;
		try{
			//CharBuffer cb = ByteBuffer.wrap(bytes).asCharBuffer();
			//result = cb.toString();
			result = new String(bytes, 0, bytes.length, "ASCII");
			result = result.trim();
			if( result.length() == 0 ){
				result = "NA";
			}
		}catch( Exception e ){
			e.printStackTrace();
			result = "NA";
		}
		// // System.out.println("Result:["+result+"]");
		return result;
	}
	
	public String getBitFromByteOld( byte data, int pos ){
		String result="0";
		char[] bitArray = null;
		String binary = null;
		try{
			// // System.out.println("Byte date to convert is ["+data+"]");
			binary   = String.format("%8s", Integer.toBinaryString(data & 0xFF)).replace(' ', '0');
			// // System.out.println("Binary is ["+binary+"]");
			bitArray = binary.toCharArray();
			result   = String.valueOf(bitArray[pos-1]);
		}catch( Exception e ){
			e.printStackTrace();
		}
		return result;
	}
	
	public String getBitFromByte( byte data, int pos ){
		String result="0";
		char[] bitArray = null;
		String binary = null;
		try{
			// // System.out.println("Byte date to convert is ["+data+"]");
			binary   = String.format("%8s", Integer.toBinaryString(data & 0xFF)).replace(' ', '0');
			//binary   = Integer.toString(data,2);
			// // System.out.println("Binary is ["+Integer.toString(data,16)+"]");
			// // System.out.println("Binary is ["+binary+"]");
			bitArray = binary.toCharArray();
			result   = String.valueOf(bitArray[bitArray.length - (pos)]);
		}catch( Exception e ){
			e.printStackTrace();
		}
		return result;
	}
	
	public String getNumberFromBCD( String hex ){
		char firstTemp;
		char secTemp;
		char[] hexArray = null;
		String result   = null;
		StringBuffer sb = null;
		try{
			sb = new StringBuffer();
			hex = hex.replace( "0", "" );
			if( hex != null && hex != "" && hex.length() > 0 ){
				// // System.out.println("Hex string is"+hex);
				hexArray = hex.toCharArray();
				for( int i = 0; i < hexArray.length; i++ ){
					firstTemp   = hexArray[i];
					// // System.out.println("First temp:"+firstTemp);
					if( (i + 1) <  hexArray.length ){
						secTemp       = hexArray[i+1];
						// // System.out.println("Second temp:"+secTemp);
						sb.append(secTemp);
						sb.append(firstTemp);
					}else
						sb.append(firstTemp);
					i++;
				}
				//sb.append(hexArray);
				result = sb.toString();
				result = result.replace("A", "0");
			}else{
				result = "0";
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		// // System.out.println("BCD is :"+result);
		return result;
	}
	
	public String getNumberFromRightBCDOld( String hex ){
		char firstTemp;
		char secTemp;
		char[] hexArray = null;
		String result   = null;
		StringBuffer sb = null;
		try{
			sb = new StringBuffer();
			hex = hex.replace( "0", "" );
			if( hex != null && hex != "" && hex.length() > 0 ){
				// // System.out.println("Hex string is"+hex);
				hexArray = hex.toCharArray();
				for( int i = hexArray.length - 1; i >= 0 ; i-- ){
					firstTemp   = hexArray[i];
					if( (i - 1) >  0 ){
						secTemp       = hexArray[i-1];
						// // System.out.println("Second temp:"+secTemp);
						sb.append(secTemp);
						sb.append(firstTemp);
					}else
						sb.append(firstTemp);
					i--;
				}
				//sb.append(hexArray);
				result = sb.toString();
				result = result.replace("A", "0");
			}else{
				result = "0";
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		// // System.out.println("BCD is :"+result);
		return result;
	}
	public String getNumberFromRightBCD( String hex ){
		char firstTemp;
		char secTemp;
		char[] hexArray = null;
		String result   = null;
		StringBuffer sb = null;
		try{
			sb = new StringBuffer();
			// // System.out.println("Hex code before replacing zero is ["+hex+"]");
			hex = hex.replace( "0", "" );
			// // System.out.println("Hex code after replacing zero is ["+hex+"]");
			if( hex != null && hex != "" && hex.length() > 0 ){
				// // System.out.println("Hex string is"+hex);
				hexArray = hex.toCharArray();
				for( int i = hexArray.length - 1; i >= 0 ; i-- ){
					if( hexArray.length % 2 == 0 ){
						firstTemp   = hexArray[i];
						secTemp       = hexArray[i-1];
						sb.append(firstTemp);
						sb.append(secTemp);
					}else{
						if( i == 0 ){
							sb.append(hexArray[i]);
						}else{
							firstTemp   = hexArray[i];
							secTemp     = hexArray[i-1];
							sb.append(firstTemp);
							sb.append(secTemp);
						}
					}
					i--;
				}
				//sb.append(hexArray);
				result = sb.toString();
				result = result.replace("A", "0");
				// // System.out.println("Right BCD result ["+result+"]");
			}else{
				result = "0";
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		// // System.out.println("BCD is :"+result);
		return result;
	}
	
	public String byteToBCD( byte[] bytes ){
		StringBuffer sb  = null;//single 
		StringBuffer sbc = null;// combined string
		try{
			sbc = new StringBuffer();
			for ( int j = 0; j < bytes.length; j++ ) {
				sb = new StringBuffer();
				byte high = (byte) (bytes[j] & 0xf0);
	        	high >>>= (byte) 4; 
	        	high = (byte) (high & 0x0f);
	        	byte low = (byte) (bytes[j] & 0x0f);
	
	        	sb.append(high);
	        	sb.append(low);
	        	sbc.append(sb.toString());
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
        return sbc.toString();
	}
	
	public String hex2Decimal( String hexCode ) {
		long decimalval = 0;
        String digits  = "0123456789ABCDEF";
        try{
        	hexCode = hexCode.toUpperCase();
        	for (int i = 0; i < hexCode.length(); i++) {
        		char c = hexCode.charAt(i);
        		int d  = digits.indexOf(c);
        		decimalval = 16*decimalval + d;
        	}
        }catch( Exception e ){
        	e.printStackTrace();
        }
        return String.valueOf(decimalval);
    }
	
	public int bcd2Decimal( String bcdCode ){
		int decimalVal = 0;
		try{
			
		}catch( Exception e ){
			e.printStackTrace();
		}
		return decimalVal;
	}

}
