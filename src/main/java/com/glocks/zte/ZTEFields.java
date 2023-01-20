package com.glocks.zte;

import java.util.HashMap;
import java.util.ArrayList;

public class ZTEFields {
////	public final String[] zteCDRFields = new String[]{"SSID","BillID","RecType","PartRecID","SeqNum","OpProp","OpNo","OpNet",
//			"OpLata","OutpOpProp","OutpOpNo","OutpOpNet","OutpOpLata","DialedNoProp","DialedNo","DialedNet","DialedLata",
//			"TpProp","TpNo","TpNet","TpLata","TpNoPrefixLen","OutpTpProp","OutpTpNo","OutpTpNet","OutpTpLata",
//			"OutpTpNoPrefixLen","AnswerTime","ServiceCat","EndTime","EndReason","OpType","ValidID","ClockID",
//			"FreeID","AttempCallID","AnswerID","AnaOpID","AnaTpID","OverseasID","InTrkGrpNo","InTrkCircuitNo",
//			"InTrkConnectTime","InTrkDisconnectTime","ORIGCLLI","ORIGMEM","InMGType","InMGID","bOpSSIPAddr",
//			"bOpMGIPAddr","bOpRtpIPAddr","bOpProtocol","bCallDirect","bCallType","bOpCoding","OutTrkGrpNo","OutTrkCircuitNo",
//			"OutTrkConnectTime","OutTrkDisconnectTime","TERMCLLI","TERMMEM","OutMGType","OutMGID","bTpSSIPAddr","bTpMGIPAddr",
//			"bTpRtpIPAddr","bTpProtocol","dwFaxPage","ChargeID","Fee","AccessNumber","CarrierID","IngresBytes","EgressBytes",
//			"BearerSvc","TeleSvc","BillNoProp","BillNo","BillNet","BillLata","TransNoProp","TransNo","TransLata",
//			"LocNoProp","LocNo","LocNet","LocLata","ChgRateKind","ChgModulatorType","ChgModulatorVal",
//			"InAttachFeeKind","InAttachFeeVal","ServiceKey","NCR","InTrkName","OutTrkName","Failurecode","InRouteCTX",
//			"OutRouteCTX","Reserved","Call_Duration","Call_Duration_Extended","Reserved2","cdr_condsider","CallDurationInSec","Changed_date","cdr_date"};
	public final String[] zteCDRFields = new String[]{"recordType","servedIMEI","servedIMSI","servedMSISDN","systemType"};
	
	public HashMap< String, int[] > getfieldSet(){
		HashMap< String, int[] > fieldsOffset = new HashMap< String, int[] >();
		try{
			// 0 -> means HEX
			// 1 -> means BCD(Calling Party Number.Zero means the end,A means 0.eg:number 89012567,HEX format is 98 1A 52 76)
			// 2 -> means BCD right align(Calling Party Lata (area code).eg:Lata 762,HEX format is 02 67)
			// 3 -> means read bit from byte
			// 4 -> means String
			fieldsOffset.put( "SSID", new int[]{ 0, 2, 0 });
			fieldsOffset.put( "BillID", new int[]{ 2, 4, 0 });
			fieldsOffset.put( "RecType", new int[]{ 6, 1, 0 });
			fieldsOffset.put( "PartRecID", new int[]{ 7, 1, 0 });
			fieldsOffset.put( "SeqNum", new int[]{ 8, 2, 0 });
			fieldsOffset.put( "OpProp", new int[]{ 10, 1, 0 });
			fieldsOffset.put( "OpNo", new int[]{ 11, 16, 1 });
			fieldsOffset.put( "OpNet", new int[]{ 27, 1, 0 });
			fieldsOffset.put( "OpLata", new int[]{ 28, 2, 2 });

			fieldsOffset.put( "OutpOpProp", new int[]{ 30, 1, 0 });
			fieldsOffset.put( "OutpOpNo", new int[]{ 31, 16, 1 });
			fieldsOffset.put( "OutpOpNet", new int[]{ 47, 1, 0 });
			fieldsOffset.put( "OutpOpLata", new int[]{ 48, 2, 2 });
			fieldsOffset.put( "DialedNoProp", new int[]{ 50, 1, 1 });
			fieldsOffset.put( "DialedNo", new int[]{ 51, 16, 1 });
			fieldsOffset.put( "DialedNet", new int[]{ 67, 1, 0 });
			fieldsOffset.put( "DialedLata", new int[]{ 68, 2, 2 });
			fieldsOffset.put( "TpProp", new int[]{ 70, 1, 0 });

			fieldsOffset.put( "TpNo", new int[]{ 71, 16, 1 });
			fieldsOffset.put( "TpNet", new int[]{ 87, 1, 0 });
			fieldsOffset.put( "TpLata", new int[]{ 88, 2, 2 });
			fieldsOffset.put( "TpNoPrefixLen", new int[]{ 90, 1, 0 });
			fieldsOffset.put( "OutpTpProp", new int[]{ 91, 1, 0 });
			fieldsOffset.put( "OutpTpNo", new int[]{ 92, 16, 1 });
			fieldsOffset.put( "OutpTpNet", new int[]{ 108, 1, 0 });
			fieldsOffset.put( "OutpTpLata", new int[]{ 109, 2, 2 });
			fieldsOffset.put( "OutpTpNoPrefixLen", new int[]{ 111, 1, 0 });
			
			fieldsOffset.put( "AnswerTime", new int[]{ 112, 8, 1 });
			fieldsOffset.put( "ServiceCat", new int[]{ 120, 1, 0 });
			fieldsOffset.put( "EndTime", new int[]{ 121, 8, 1 });
			fieldsOffset.put( "EndReason", new int[]{ 129, 1, 0 });
			fieldsOffset.put( "OpType", new int[]{ 130, 1, 0 });
			fieldsOffset.put( "ValidID", new int[]{ 131, 1, 3 });
			fieldsOffset.put( "ClockID", new int[]{ 131, 2, 3 });
			fieldsOffset.put( "FreeID", new int[]{ 131, 3, 3 });
			fieldsOffset.put( "AttempCallID", new int[]{ 131, 4, 3 });			

			fieldsOffset.put( "AnswerID", new int[]{ 131, 5, 3 });
			fieldsOffset.put( "AnaOpID", new int[]{ 131, 6, 3 });
			fieldsOffset.put( "AnaTpID", new int[]{ 131, 7 , 3});
			fieldsOffset.put( "OverseasID", new int[]{ 131, 8, 3 });
			fieldsOffset.put( "InTrkGrpNo", new int[]{ 132, 2, 0 });
			fieldsOffset.put( "InTrkCircuitNo", new int[]{ 134, 2, 0 });
			fieldsOffset.put( "InTrkConnectTime", new int[]{ 136, 8, 1 });
			fieldsOffset.put( "InTrkDisconnectTime", new int[]{ 144, 8, 1 });
			fieldsOffset.put( "ORIGCLLI", new int[]{ 152, 3, 0 });			

			fieldsOffset.put( "ORIGMEM", new int[]{ 155, 3, 0 });
			fieldsOffset.put( "InMGType", new int[]{ 158, 1, 0 });
			fieldsOffset.put( "InMGID", new int[]{ 159, 2, 0 });
			fieldsOffset.put( "bOpSSIPAddr", new int[]{ 161, 4, 0 });
			fieldsOffset.put( "bOpMGIPAddr", new int[]{ 165, 4, 0 });
			fieldsOffset.put( "bOpRtpIPAddr", new int[]{ 169, 4, 0 });
			fieldsOffset.put( "bOpProtocol", new int[]{ 173, 1, 0 });
			fieldsOffset.put( "bCallDirect", new int[]{ 174, 1, 0 });
			fieldsOffset.put( "bCallType", new int[]{ 175, 1, 0 });			

			fieldsOffset.put( "bOpCoding", new int[]{ 176, 1, 0 });
			fieldsOffset.put( "OutTrkGrpNo", new int[]{ 177, 2, 0 });
			fieldsOffset.put( "OutTrkCircuitNo", new int[]{ 179, 2, 0 });
			fieldsOffset.put( "OutTrkConnectTime", new int[]{ 181, 8, 1 });
			fieldsOffset.put( "OutTrkDisconnectTime", new int[]{ 189, 8, 1 });
			fieldsOffset.put( "TERMCLLI", new int[]{ 197, 3, 0 });
			fieldsOffset.put( "TERMMEM", new int[]{ 200, 3, 0 });
			fieldsOffset.put( "OutMGType", new int[]{ 203, 1, 0 });
			fieldsOffset.put( "OutMGID", new int[]{ 204, 2, 0 });			

			fieldsOffset.put( "bTpSSIPAddr", new int[]{ 206, 4, 0 });
			fieldsOffset.put( "bTpMGIPAddr", new int[]{ 210, 4, 0 });
			fieldsOffset.put( "bTpRtpIPAddr", new int[]{ 214, 4, 0 });
			fieldsOffset.put( "bTpProtocol", new int[]{ 218, 1, 0 });
			fieldsOffset.put( "dwFaxPage", new int[]{ 219, 4, 0 });
			fieldsOffset.put( "ChargeID", new int[]{ 223, 1, 0 });
			fieldsOffset.put( "Fee", new int[]{ 224, 4, 1 });
			fieldsOffset.put( "AccessNumber", new int[]{ 228, 7, 1 });
			fieldsOffset.put( "CarrierID", new int[]{ 235, 2, 1 });			

			fieldsOffset.put( "IngresBytes", new int[]{ 237, 8, 0 });
			fieldsOffset.put( "EgressBytes", new int[]{ 245, 8, 0 });
			fieldsOffset.put( "BearerSvc", new int[]{ 253, 1, 1 });
			fieldsOffset.put( "TeleSvc", new int[]{ 254, 1, 1 });
			fieldsOffset.put( "BillNoProp", new int[]{ 255, 1, 0 });
			fieldsOffset.put( "BillNo", new int[]{ 256, 16, 1 });
			fieldsOffset.put( "BillNet", new int[]{ 272, 1, 0 });
			fieldsOffset.put( "BillLata", new int[]{ 273, 2, 2 });
			fieldsOffset.put( "TransNoProp", new int[]{ 275, 1, 0 });			

			fieldsOffset.put( "TransNo", new int[]{ 276, 16, 1 });
			fieldsOffset.put( "TransLata", new int[]{ 292, 2, 2 });
			fieldsOffset.put( "LocNoProp", new int[]{ 294, 1, 0 });
			fieldsOffset.put( "LocNo", new int[]{ 295, 16, 1 });
			fieldsOffset.put( "LocNet", new int[]{ 311, 1, 0 });
			fieldsOffset.put( "LocLata", new int[]{ 312, 2, 2 });
			fieldsOffset.put( "ChgRateKind", new int[]{ 314, 2, 2 });
			fieldsOffset.put( "ChgModulatorType", new int[]{ 316,1, 0 });
			fieldsOffset.put( "ChgModulatorVal", new int[]{ 317, 2, 0 });

			fieldsOffset.put( "InAttachFeeKind", new int[]{ 319, 1, 1 });
			fieldsOffset.put( "InAttachFeeVal", new int[]{ 320, 4, 1 });
			fieldsOffset.put( "ServiceKey", new int[]{ 324, 4, 0 });
			fieldsOffset.put( "NCR", new int[]{ 328, 29, 0 });
			fieldsOffset.put( "InTrkName", new int[]{ 357, 7, 4 });
			fieldsOffset.put( "OutTrkName", new int[]{ 364, 7, 4 });
			fieldsOffset.put( "Failurecode", new int[]{ 371, 1, 0 });
			fieldsOffset.put( "InRouteCTX", new int[]{ 372, 2, 0 });
			fieldsOffset.put( "OutRouteCTX", new int[]{ 374, 2, 0 });
			fieldsOffset.put( "Reserved", new int[]{ 376, 2, 0 });
			fieldsOffset.put( "Call_Duration", new int[]{ 378, 4, 1 });
			fieldsOffset.put( "Call_Duration_Extended", new int[]{ 382, 1, 1 });
			fieldsOffset.put( "Reserved2", new int[]{ 383, 9, 0 });
		}catch( Exception e ){
			e.printStackTrace();
		}
		return fieldsOffset;
	}
}
