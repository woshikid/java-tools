package com.github.woshikid.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 遍历所有可能性的
 * 五子棋算法
 * @author kid
 *
 */
public class FiveAll implements Runnable{
	public void run(){
		try{
			while(true){
				System.out.print("Command:");
				String s=input.readLine();
				if("s".equals(s)){
					status();
				}else if("q".equals(s)){
					pause();
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			pause();
		}
	}
	
	public FiveAll()throws Exception{
		init();
	}
	
	public void start(boolean cut,int colour)throws Exception{
		if(colour!=1 && colour!=2)throw new Exception("colour illegal!");
		if(pause){
			handcut=cut;
			mcolour=colour;
			pause=false;
			mmap[7][7]=1;
			mmap[6][6]=2;
			new Thread(this).start();
			calculate(mmap,0,-1);
			System.out.print("\n\nOK\n\n");
		}else{
			System.out.print("\n\nAlready working\n\n");
		}
	}
	
	public void status(){
		System.out.print("\nstatus:\n");
		for(int i=0;i<length*height;i++){
			System.out.print(status[i]+" ");
		}
		System.out.print("\nadded:"+added);
		System.out.print("\ndeleted:"+deleted);
		System.out.print("\nremain:"+(added-deleted));
		System.out.print("\n\n");
	}
	
	public void pause(){
		pause=true;
	}
	
	private BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	private boolean handcut=false;
	private Connection con;
	private int length=15;
	private int height=15;
	private int mmap[][];
	private int order[][];
	private int status[];
	private int mcolour=1;
	private int mtxy[][];
	private int mfxy[][];
	private final int valueMax=2147483647;
	private final int valueMin=-2147483647;
	private final int valueWin=1000000000;
	private final int valueStep=10000;
	private static boolean pause=true;
	private long added=0;
	private long deleted=0;
	
	private String id(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int i=0;i<length;i++){
			for(int j=0;j<height;j++){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private String id2(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int i=0;i<length;i++){
			for(int j=height-1;j>=0;j--){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private String id3(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int i=length-1;i>=0;i--){
			for(int j=0;j<height;j++){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private String id4(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int i=length-1;i>=0;i--){
			for(int j=height-1;j>=0;j--){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private String id5(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int j=0;j<height;j++){
			for(int i=0;i<length;i++){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private String id6(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int j=0;j<height;j++){
			for(int i=length-1;i>=0;i--){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private String id7(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int j=height-1;j>=0;j--){
			for(int i=0;i<length;i++){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private String id8(int omap[][])throws Exception{
		StringBuffer buffer=new StringBuffer();
		for(int j=height-1;j>=0;j--){
			for(int i=length-1;i>=0;i--){
				buffer.append(omap[i][j]);
			}
		}
		if(buffer.length()!=225)throw new Exception("id length="+buffer.length());
		return buffer.toString();
	}
	
	private void init()throws Exception{
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/fiveall";
		String user = "root";
		String password = "kidkid";
		Class.forName(driver);
		con = DriverManager.getConnection(url, user, password);
		if(con.isClosed())throw new Exception("can't connect to the database");
		mmap=new int[length][height];
		status=new int[length*height];
		order=new int[length*height][2];
		order[0][0]=7;
		order[0][1]=7;
		int index=1;
		for(int step=1;step<=7;step++){
			for(int i=7-step;i<=7+step;i++){
				order[index][0]=i;
				order[index][1]=7-step;
				index++;
				order[index][0]=i;
				order[index][1]=7+step;
				index++;
			}
			for(int i=7-step+1;i<=7+step-1;i++){
				order[index][0]=7-step;
				order[index][1]=i;
				index++;
				order[index][0]=7+step;
				order[index][1]=i;
				index++;
			}
		}
	}
	
	private void mapCopy(int source[][],int des[][]){
		for(int i=0;i<length;i++)
			for(int j=0;j<height;j++)
				des[i][j]=source[i][j];
	}

	private int stopFour(){
		for(int i=0;i<4;i++){
			if(mfxy[i][0]==1){
				return mfxy[i][1]*height+mfxy[i][2];
			}
		}
		return -1;
	}
	
	private boolean checkWin(int colour,int[][] omap,int x,int y){
		//line |
		int middle=1;
		int j=y-1;
		while(j>=0){
			if(omap[x][j]==colour){
				middle++;
			}else{
				break;
			}
			j--;
		}

		j=y+1;
		while(j<height){
			if(omap[x][j]==colour){
				middle++;
			}else{
				break;
			}
			j++;
		}

		if(middle==5)return true;
		if(middle>5){
			if(colour==2)return true;
			if(!handcut)return true;
		}

		//line -
		middle=1;
		int i=x-1;
		while(i>=0){
			if(omap[i][y]==colour){
				middle++;
			}else{
				break;
			}
			i--;
		}

		i=x+1;
		while(i<length){
			if(omap[i][y]==colour){
				middle++;
			}else{
				break;
			}
			i++;
		}

		if(middle==5)return true;
		if(middle>5){
			if(colour==2)return true;
			if(!handcut)return true;
		}

		//line /
		middle=1;
		i=x-1;
		j=y+1;
		while(i>=0 && j<height){
			if(omap[i][j]==colour){
				middle++;
			}else{
				break;
			}
			i--;
			j++;
		}

		i=x+1;
		j=y-1;
		while(i<length && j>=0){
			if(omap[i][j]==colour){
				middle++;
			}else{
				break;
			}
			i++;
			j--;
		}

		if(middle==5)return true;
		if(middle>5){
			if(colour==2)return true;
			if(!handcut)return true;
		}

		//line \
		middle=1;
		i=x-1;
		j=y-1;
		while(i>=0 && j>=0){
			if(omap[i][j]==colour){
				middle++;
			}else{
				break;
			}
			i--;
			j--;
		}

		i=x+1;
		j=y+1;
		while(i<length && j<height){
			if(omap[i][j]==colour){
				middle++;
			}else{
				break;
			}
			i++;
			j++;
		}

		if(middle==5)return true;
		if(middle>5){
			if(colour==2)return true;
			if(!handcut)return true;
		}

		return false;
	}
	
	private int findWin(int colour,int[][] omap){		
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(omap[i][j]!=0)continue;
			if(checkWin(colour,omap,i,j))return i*height+j;
		}
		return -1;
	}
	
	private boolean checkEmpty(int[][] omap,int x,int y){
		for(int i=x-2;i<=x+2;i++){
			if(i<0 || i>=length)continue;
			for(int j=y-2;j<=y+2;j++){
				if(j<0 || j>=height)continue;
				if(omap[i][j]!=0)return false;
			}
		}
		return true;
	}
	
	private int scanMap(int colour,int[][] omap,int x,int y,boolean checkonly){
		if(checkonly && (colour==2 || !handcut))return 1;
		int cthree=0;
		int three=0;
		int four=0;
		boolean lfour=false;
		boolean cuted=false;
		int tvalue=0;

		mtxy=new int[4][5];
		mfxy=new int[4][3];
		for(int i=0;i<4;i++){
			int value=0;
			if(i==0)value=scan0(colour,omap,x,y,true);
			if(i==1)value=scan1(colour,omap,x,y,true);
			if(i==2)value=scan2(colour,omap,x,y,true);
			if(i==3)value=scan3(colour,omap,x,y,true);
			if(value==valueMax)return valueMax;
			if(value==valueMin){
				cuted=true;
			}else{
				tvalue=tvalue+value;
				if(value==300)lfour=true;
				if(value==300 || value==54)four++;
				if(value==53)three++;
			}
		}
		int txy[][]=new int[4][5];
		int fxy[][]=new int[4][3];
		for(int i=0;i<4;i++){
			for(int j=0;j<3;j++){
				txy[i][j]=mtxy[i][j];
				fxy[i][j]=mfxy[i][j];
			}
			for(int j=3;j<5;j++)
				txy[i][j]=mtxy[i][j];
		}

		if(colour==1 && handcut==true){
			if(cuted)return valueMin;
			if(four>1)return valueMin;
			if(checkonly && three<2)return 1;
			if(three>0){
				int cmap[][]=new int[length][height];
				mapCopy(omap,cmap);
				cmap[x][y]=1;
				for(int i=0;i<4;i++){
					if(checkonly && (cthree+three)<2)return 1;
					if(txy[i][0]==0)continue;
					three--;
					tvalue=tvalue-53;
					if(txy[i][0]==1){
						if(scanMap(1,cmap,txy[i][1],txy[i][2],true)==valueMin)continue;
					}else if(txy[i][0]==2){
						if(scanMap(1,cmap,txy[i][3],txy[i][4],true)==valueMin)continue;
					}else{
						if(scanMap(1,cmap,txy[i][1],txy[i][2],true)==valueMin && scanMap(1,cmap,txy[i][3],txy[i][4],true)==valueMin)continue;
					}
					cthree++;
					if(cthree>1)return valueMin;
					tvalue=tvalue+53;
				}
				if(checkonly)return 1;
				three=cthree;
			}
		}
		
		for(int i=0;i<4;i++){
			for(int j=0;j<3;j++){
				mtxy[i][j]=txy[i][j];
				mfxy[i][j]=fxy[i][j];
			}
			for(int j=3;j<5;j++)
				mtxy[i][j]=txy[i][j];
		}
		if(lfour==true || four>1)tvalue=tvalue+valueWin;
		return tvalue;
	}
	
	private int calValue(int colour,int[][] omap,int x,int y,int type,int middle,int s1,int e1,int x1,int y1,int s2,int e2,int x2,int y2,boolean record){
		boolean four1=false;
		boolean four2=false;
		boolean three=false;
		boolean hthree=false;

		if(middle>5){
			if(colour==2)return valueMax;
			if(!handcut)return valueMax;
			return valueMin;
		}
		if(middle==5)return valueMax;
		if((middle+e1)>4 && (colour==2 || !handcut))four1=true;
		if((middle+e2)>4 && (colour==2 || !handcut))four2=true;
		if((middle+e1)==4){
			if(middle==4){
				if(s1>0)four1=true;
			}else{
				four1=true;
			}
		}
		if((middle+e2)==4){
			if(middle==4){
				if(s2>0)four2=true;
			}else{
				four2=true;
			}
		}
		if(four1==true && four2==true){//活四
			if(colour==2)return 300;
			if(!handcut)return 300;
			if(middle==4)return 300;
			return valueMin;
		}
		int cmap[][]=new int[length][height];
		mapCopy(omap,cmap);
		cmap[x][y]=colour;
		if(four1==true || four2==true){//冲四
			if(four1==true){
				if(colour==2 && handcut==true && scanMap(1,cmap,x1,y1,true)==valueMin)return 300;
				if(record==true){
					mfxy[type][0]=1;
					mfxy[type][1]=x1;
					mfxy[type][2]=y1;
				}
			}else{
				if(colour==2 && handcut==true && scanMap(1,cmap,x2,y2,true)==valueMin)return 300;
				if(record==true){
					mfxy[type][0]=1;
					mfxy[type][1]=x2;
					mfxy[type][2]=y2;
				}
			}
			return 54;
		}
		if((middle+e1)==3 && s1>0){
			int value=0;
			if(type==0)value=scan0(colour,cmap,x1,y1,false);
			if(type==1)value=scan1(colour,cmap,x1,y1,false);
			if(type==2)value=scan2(colour,cmap,x1,y1,false);
			if(type==3)value=scan3(colour,cmap,x1,y1,false);
			if(value==54)hthree=true;
			if(value==300){
				three=true;
				if(record==true){
					mtxy[type][0]+=1;
					mtxy[type][1]=x1;
					mtxy[type][2]=y1;
				}
			}
		}
		if((middle+e2)==3 && s2>0){
			int value=0;
			if(type==0)value=scan0(colour,cmap,x2,y2,false);
			if(type==1)value=scan1(colour,cmap,x2,y2,false);
			if(type==2)value=scan2(colour,cmap,x2,y2,false);
			if(type==3)value=scan3(colour,cmap,x2,y2,false);
			if(value==54)hthree=true;
			if(value==300){
				three=true;
				if(record==true){
					mtxy[type][0]+=2;
					mtxy[type][3]=x2;
					mtxy[type][4]=y2;
				}
			}
		}
		if(three==true)return 53;//活三
		if(hthree==true)return 3;//眠三
		if(middle==1 && e1==1 && e2==1)return 3;//眠三
		if((middle+e1)==2 && s1==2){
			if(e2>0)return 2;//眠二
			if(s2==2)return 5;//活二
			if(s2>0)return 4;//半活二
			return 2;//眠二
		}
		if((middle+e2)==2 && s2==2){
			if(e1>0)return 2;//眠二
			if(s1==2)return 5;//活二
			if(s1>0)return 4;//半活二
			return 2;//眠二
		}
		if((middle+e1+e2)==1 && (s1+s2)==4)return 1;//活一
		return 0;
	}

	//line |
	private int scan0(int colour,int[][] omap,int x,int y,boolean record){
		int middle=1;
		int x1=0;
		int s1=0;
		int e1=0;
		int x2=0;
		int s2=0;
		int e2=0;
		int j=y-1;
		while(j>=0){
			if(omap[x][j]==colour){
				if(s1==0){
					x1++;
				}else{
					e1++;
				}
			}else if(omap[x][j]==0){
				s1++;
				if(s1==2)break;
			}else{
				break;
			}
			j--;
		}

		j=y+1;
		while(j<height){
			if(omap[x][j]==colour){
				if(s2==0){
					x2++;
				}else{
					e2++;
				}
			}else if(omap[x][j]==0){
				s2++;
				if(s2==2)break;
			}else{
				break;
			}
			j++;
		}

		middle=middle+x1+x2;
		return calValue(colour,omap,x,y,0,middle,s1,e1,x,y-1-x1,s2,e2,x,y+1+x2,record);
	}

	//line -
	private int scan1(int colour,int[][] omap,int x,int y,boolean record){
		int middle=1;
		int x1=0;
		int s1=0;
		int e1=0;
		int x2=0;
		int s2=0;
		int e2=0;
		int i=x-1;
		while(i>=0){
			if(omap[i][y]==colour){
				if(s1==0){
					x1++;
				}else{
					e1++;
				}
			}else if(omap[i][y]==0){
				s1++;
				if(s1==2)break;
			}else{
				break;
			}
			i--;
		}

		i=x+1;
		while(i<length){
			if(omap[i][y]==colour){
				if(s2==0){
					x2++;
				}else{
					e2++;
				}
			}else if(omap[i][y]==0){
				s2++;
				if(s2==2)break;
			}else{
				break;
			}
			i++;
		}

		middle=middle+x1+x2;
		return calValue(colour,omap,x,y,1,middle,s1,e1,x-1-x1,y,s2,e2,x+1+x2,y,record);
	}

	//line /
	private int scan2(int colour,int[][] omap,int x,int y,boolean record){
		int middle=1;
		int x1=0;
		int s1=0;
		int e1=0;
		int x2=0;
		int s2=0;
		int e2=0;
		int i=x-1;
		int j=y+1;
		while(i>=0 && j<height){
			if(omap[i][j]==colour){
				if(s1==0){
					x1++;
				}else{
					e1++;
				}
			}else if(omap[i][j]==0){
				s1++;
				if(s1==2)break;
			}else{
				break;
			}
			i--;
			j++;
		}

		i=x+1;
		j=y-1;
		while(i<length && j>=0){
			if(omap[i][j]==colour){
				if(s2==0){
					x2++;
				}else{
					e2++;
				}
			}else if(omap[i][j]==0){
				s2++;
				if(s2==2)break;
			}else{
				break;
			}
			i++;
			j--;
		}

		middle=middle+x1+x2;
		return calValue(colour,omap,x,y,2,middle,s1,e1,x-1-x1,y+1+x1,s2,e2,x+1+x2,y-1-x2,record);
	}

	//line \
	private int scan3(int colour,int[][] omap,int x,int y,boolean record){
		int middle=1;
		int x1=0;
		int s1=0;
		int e1=0;
		int x2=0;
		int s2=0;
		int e2=0;
		int i=x-1;
		int j=y-1;
		while(i>=0 && j>=0){
			if(omap[i][j]==colour){
				if(s1==0){
					x1++;
				}else{
					e1++;
				}
			}else if(omap[i][j]==0){
				s1++;
				if(s1==2)break;
			}else{
				break;
			}
			i--;
			j--;
		}

		i=x+1;
		j=y+1;
		while(i<length && j<height){
			if(omap[i][j]==colour){
				if(s2==0){
					x2++;
				}else{
					e2++;
				}
			}else if(omap[i][j]==0){
				s2++;
				if(s2==2)break;
			}else{
				break;
			}
			i++;
			j++;
		}

		middle=middle+x1+x2;
		return calValue(colour,omap,x,y,3,middle,s1,e1,x-1-x1,y-1-x1,s2,e2,x+1+x2,y+1+x2,record);
	}
	
	private int selectDB(String id)throws Exception{
		int value=-1;
		Statement st=con.createStatement();
		ResultSet rs;
		if(handcut){
			rs=st.executeQuery("select value from handcut where id='"+id+"'");
		}else{
			rs=st.executeQuery("select value from handfree where id='"+id+"'");
		}
		if(rs.next())value=rs.getInt(1);
		rs.close();
		st.close();
		return value;
	}
	
	private int calculate(int[][] omap,int deep,int ij)throws Exception{
		if(pause){
			status();
			System.exit(0);
		}
		
		String id=id(omap);
		int value=selectDB(id);
		if(value>=0)return value;
		value=selectDB(id2(omap));
		if(value>=0)return value;
		value=selectDB(id3(omap));
		if(value>=0)return value;
		value=selectDB(id4(omap));
		if(value>=0)return value;
		value=selectDB(id5(omap));
		if(value>=0)return value;
		value=selectDB(id6(omap));
		if(value>=0)return value;
		value=selectDB(id7(omap));
		if(value>=0)return value;
		value=selectDB(id8(omap));
		if(value>=0)return value;
		
		int total=0;
		int scaned=0;
		int values[][]=new int[length][height];
		int ijs[][]=new int[length][height];
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(ij>=0 && (i*height+j)!=ij)continue;
			if(omap[i][j]!=0)continue;
			if(checkEmpty(omap,i,j))continue;
			value=scanMap(mcolour,omap,i,j,false);
			ijs[i][j]=stopFour();
			if(value>=valueWin){
				return 19999;
			}else if(value==valueMin){
				values[i][j]=1;
			}else if(value<53){
				values[i][j]=2;
			}else if(value>100){
				values[i][j]=5;
			}else if(ijs[i][j]>=0){
				values[i][j]=4;
			}else{
				values[i][j]=3;
			}
			total++;
		}
		
		int maxv=-1;
		int maxi=-1;
		int maxj=-1;
		boolean exit=false;
		for(int k=5;k>0;k--){
			if(exit)break;
			for(int o=0;o<(length*height);o++){
				if(exit)break;
				int i=order[o][0];
				int j=order[o][1];
				if(values[i][j]!=k)continue;
				scaned++;
				status[deep]=(scaned*10-1)/total;
				if(values[i][j]==1){
					if(1>maxv){
						maxv=1;
						maxi=i;
						maxj=j;
					}
					continue;
				}
				int cmap[][]=new int[length][height];
				mapCopy(omap,cmap);
				cmap[i][j]=mcolour;
				
				values[i][j]=-1;//for delete tree
				
				int total2=0;
				int scaned2=0;
				int values2[][]=new int[length][height];
				int ijs2[][]=new int[length][height];
				int o2=0;
				for(o2=0;o2<(length*height);o2++){
					int i2=order[o2][0];
					int j2=order[o2][1];
					if(ijs[i][j]>=0 && (i2*height+j2)!=ijs[i][j])continue;
					if(cmap[i2][j2]!=0)continue;
					value=scanMap(mcolour==1?2:1,cmap,i2,j2,false);
					ijs2[i2][j2]=stopFour();
					if(value>=valueWin){
						break;
					}else if(value==valueMin){
						values2[i2][j2]=1;
					}else if(value<53){
						values2[i2][j2]=2;
					}else if(value>100){
						values2[i2][j2]=5;
					}else if(ijs2[i2][j2]>=0){
						values2[i2][j2]=4;
					}else{
						values2[i2][j2]=3;
					}
					total2++;
				}
				if(o2<(length*height)){
					if(2>maxv){
						maxv=2;
						maxi=i;
						maxj=j;
					}
					continue;
				}
				
				int minv=20001;
				boolean exit2=false;
				for(int k2=5;k2>0;k2--){
					if(exit2)break;
					for(o2=0;o2<(length*height);o2++){
						if(exit2)break;
						int i2=order[o2][0];
						int j2=order[o2][1];
						if(values2[i2][j2]!=k2)continue;
						scaned2++;
						status[deep+1]=(scaned2*10-1)/total2;
						if(values2[i2][j2]==1){
							if(20000<minv)minv=20000;
							continue;
						}
						int dmap[][]=new int[length][height];
						mapCopy(cmap,dmap);
						dmap[i2][j2]=(mcolour==1?2:1);
						
						value=calculate(dmap,deep+2,ijs2[i2][j2]);
						if(value<minv)minv=value;
						if(minv<10000)exit2=true;
					}
				}
				if(minv==20001)minv=10000;
				if(minv>10000)minv--;
				if(minv<10000)minv++;
				if(minv>maxv){
					maxv=minv;
					maxi=i;
					maxj=j;
				}
				if(maxv>10000)exit=true;
			}
		}
		if(maxv==-1)return 10000;
		//cleanTree(omap,maxi,maxj,values);
		Statement st=con.createStatement();
		if(handcut){
			st.executeUpdate("insert into handcut(id,x,y,value) values('"+id+"',"+maxi+","+maxj+","+maxv+")");
		}else{
			st.executeUpdate("insert into handfree(id,x,y,value) values('"+id+"',"+maxi+","+maxj+","+maxv+")");
		}
		st.close();
		added++;
		return maxv;
	}
	
	private void cleanTree(int[][] omap,int x,int y,int[][] values)throws Exception{
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(values[i][j]!=-1)continue;
			if(i==x && j==y)continue;
			int cmap[][]=new int[length][height];
			mapCopy(omap,cmap);
			cmap[i][j]=mcolour;
			for(int s=0;s<(length*height);s++){
				int si=order[s][0];
				int sj=order[s][1];
				if(cmap[si][sj]!=0)continue;
				int dmap[][]=new int[length][height];
				mapCopy(cmap,dmap);
				dmap[si][sj]=(mcolour==1?2:1);
				deleteTree(dmap);
			}
		}
	}
	
	private void deleteTree(int[][] omap)throws Exception{
		String id=id(omap);
		Statement st=con.createStatement();
		ResultSet rs;
		if(handcut){
			rs=st.executeQuery("select x,y from handcut where id='"+id+"'");
		}else{
			rs=st.executeQuery("select x,y from handfree where id='"+id+"'");
		}
		while(rs.next()){
			int x=rs.getInt(1);
			int y=rs.getInt(2);
			int cmap[][]=new int[length][height];
			mapCopy(omap,cmap);
			cmap[x][y]=mcolour;
			for(int s=0;s<(length*height);s++){
				int si=order[s][0];
				int sj=order[s][1];
				if(cmap[si][sj]!=0)continue;
				int dmap[][]=new int[length][height];
				mapCopy(cmap,dmap);
				dmap[si][sj]=(mcolour==1?2:1);
				deleteTree(dmap);
			}
		}
		rs.close();
		st.close();
		st=con.createStatement();
		if(handcut){
			deleted+=st.executeUpdate("delete from handcut where id='"+id+"'");
		}else{
			deleted+=st.executeUpdate("delete from handfree where id='"+id+"'");
		}
		st.close();
	}
	
	public static void main(String args[])throws Exception{
		FiveAll fa=new FiveAll();
		fa.start(false,1);
	}
}
