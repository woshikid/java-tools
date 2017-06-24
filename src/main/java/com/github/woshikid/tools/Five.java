package com.github.woshikid.tools;

/**
 * 五子棋算法
 * @author kid
 *
 */
public class Five {
	private int length=0;
	private int height=0;
	private int mmap[][];
	private int order[][];
	private int mdeep=0;
	private boolean handcut=false;
	private final int valueMax=2147483647;
	private final int valueMin=-2147483647;
	private final int valueWin=1000000000;
	private final int valueStep=10000;
	private int nextPoint=-1;
	private int mtxy[][];
	private int mfxy[][];
	private int smain;
	private int sdetail[];

	public Five()throws Exception{
		length=15;
		height=15;
		mmap=new int[length][height];
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
		sdetail=new int[100];
	}

	public boolean put(int x,int y,int colour){
		if(x>=length || x<0 || y>=height || y<0)return false;
		if(colour!=1 && colour!=2)return false;
		if(mmap[x][y]!=0)return false;
		mmap[x][y]=colour;
		return true;
	}

	public void changeDeep(int deep){
		if(deep<0)return;
		mdeep=deep;
	}

	public void setHandcut(boolean handcut){
		this.handcut=handcut;
	}
	
	public int readX(){
		return nextPoint/height;
	}
	
	public int readY(){
		return nextPoint%height;
	}
	
	//black=1,white=2
	public boolean getPoint(int colour){
		if(colour!=1 && colour!=2)return false;
		nextPoint=getPoint(colour,mmap);
		return nextPoint<0?false:true;
	}
	
	private void mapCopy(int source[][],int des[][]){
		for(int i=0;i<length;i++)
			for(int j=0;j<height;j++)
				des[i][j]=source[i][j];
	}

	private int getPoint(int colour,int[][] omap){
		//pre scan
		smain=0;
		int ij=findWin(colour,omap);
		if(ij>=0)return ij;
		int value=0;
		int cmap[][]=new int[length][height];
		ij=findWin(colour==1?2:1,omap);
		if(ij>=0){
			value=scanMap(colour,omap,ij/height,ij%height,false);
			if(value==valueMin)return -1;
			if(value>=valueWin)return ij;
			value=stopFour();
			if(scanWin(colour,omap,0,ij,findThree(colour,omap))>=0)return ij;
			mapCopy(omap,cmap);
			cmap[ij/height][ij%height]=colour;
			if(scanWin(colour==1?2:1,cmap,0,value,findThree(colour==1?2:1,cmap))>=0)return -1;
			return ij;
		}
		ij=scanWin(colour,omap,0,-1,findThree(colour,omap));
		if(ij>=0)return ij;
		//first scan
		int values[][]=new int[length][height];
		int ijs[][]=new int[length][height];
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(omap[i][j]!=0)continue;
			if(checkEmpty(omap,i,j))continue;
			value=scanMap(colour,omap,i,j,false);
			if(value==valueMin)continue;
			ijs[i][j]=stopFour();
			if(value<53){
				values[i][j]=1;
			}else if(value>100){
				values[i][j]=4;
			}else if(ijs[i][j]>=0){
				values[i][j]=3;
			}else{
				values[i][j]=2;
			}
		}
		//deep scan
		for(int k=4;k>0;k--){
			for(int o=0;o<(length*height);o++){
				int i=order[o][0];
				int j=order[o][1];
				if(values[i][j]!=k)continue;
				smain=o+1;
				mapCopy(omap,cmap);
				cmap[i][j]=colour;
				if(scanWin(colour==1?2:1,cmap,0,ijs[i][j],findThree(colour==1?2:1,cmap))>=0)continue;
				if(ijs[i][j]<0){
					return i*height+j;
				}else{
					cmap[ijs[i][j]/height][ijs[i][j]%height]=colour==1?2:1;
					if(getPoint(colour,cmap)>=0)return i*height+j;
				}
				/*
				//cal turns
				if(index<mdeep){//how smart the opp is
					int nextColour=colour;
					int turn=mturn;
					for(int k=0;k<turn;k++){//how smart you are
						nextColour=nextColour==1?2:1;
						ij=getPoint(nextColour,cmap,index+1);
						if(ij<0 && nextValue==0)break;
						if(nextValue==0)turn=turn+2;
						if(nextValue==valueMin){
							value=nextColour==colour?(valueMin+valueStep*k):(valueMax-valueStep*k);
							break;
						}
						if(nextValue>=valueWin){
							value=nextColour==colour?(nextValue-valueStep*k):(valueStep*k-nextValue);
							break;
						}
						cmap[ij/height][ij%height]=nextColour;
					}
				}
				//choose best one
				if(value>maxv){
					maxv=value;
					maxi=i;
					maxj=j;
				}
				*/
			}
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

	private int findWin(int colour,int[][] omap){		
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(omap[i][j]!=0)continue;
			if(checkWin(colour,omap,i,j))return i*height+j;
		}
		return -1;
	}

	/*
	private boolean nextWin(int colour,int[][] omap,int x,int y){
		int cmap[][]=new int[length][height];
		mapCopy(omap,cmap);
		cmap[x][y]=colour;
		int txy[][]=new int[4][5];
		boolean nextWin=false;
		//line |
		boolean s=false;
		int j=y-1;
		while(j>=0){
			if(omap[x][j]==colour){
			}else if(omap[x][j]==0){
				if(scanMap(colour,cmap,x,j,false)>100){
					txy[0][0]+=1;
					txy[0][1]=x;
					txy[0][2]=j;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			j--;
		}

		s=false;
		j=y+1;
		while(j<height){
			if(omap[x][j]==colour){
			}else if(omap[x][j]==0){
				if(scanMap(colour,cmap,x,j,false)>100){
					txy[0][0]+=2;
					txy[0][3]=x;
					txy[0][4]=j;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			j++;
		}

		//line -
		s=false;
		int i=x-1;
		while(i>=0){
			if(omap[i][y]==colour){
			}else if(omap[i][y]==0){
				if(scanMap(colour,cmap,i,y,false)>100){
					txy[1][0]+=1;
					txy[1][1]=i;
					txy[1][2]=y;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			i--;
		}

		s=false;
		i=x+1;
		while(i<length){
			if(omap[i][y]==colour){
			}else if(omap[i][y]==0){
				if(scanMap(colour,cmap,i,y,false)>100){
					txy[1][0]+=2;
					txy[1][3]=i;
					txy[1][4]=y;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			i++;
		}

		//line /
		s=false;
		i=x-1;
		j=y+1;
		while(i>=0 && j<height){
			if(omap[i][j]==colour){
			}else if(omap[i][j]==0){
				if(scanMap(colour,cmap,i,j,false)>100){
					txy[2][0]+=1;
					txy[2][1]=i;
					txy[2][2]=j;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			i--;
			j++;
		}

		s=false;
		i=x+1;
		j=y-1;
		while(i<length && j>=0){
			if(omap[i][j]==colour){
			}else if(omap[i][j]==0){
				if(scanMap(colour,cmap,i,j,false)>100){
					txy[2][0]+=2;
					txy[2][3]=i;
					txy[2][4]=j;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			i++;
			j--;
		}

		//line \
		s=false;
		i=x-1;
		j=y-1;
		while(i>=0 && j>=0){
			if(omap[i][j]==colour){
			}else if(omap[i][j]==0){
				if(scanMap(colour,cmap,i,j,false)>100){
					txy[3][0]+=1;
					txy[3][1]=i;
					txy[3][2]=j;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			i--;
			j--;
		}

		s=false;
		i=x+1;
		j=y+1;
		while(i<length && j<height){
			if(omap[i][j]==colour){
			}else if(omap[i][j]==0){
				if(scanMap(colour,cmap,i,j,false)>100){
					txy[3][0]+=2;
					txy[3][3]=i;
					txy[3][4]=j;
					nextWin=true;
					break;
				}
				if(s==true)break;
				s=true;
			}else{
				break;
			}
			i++;
			j++;
		}

		for(i=0;i<4;i++)
		for(j=0;j<5;j++)
			mtxy[i][j]=txy[i][j];

		return nextWin;
	}
	*/

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

	//找到阻止冲四的点
	private int stopFour(){
		for(int i=0;i<4;i++){
			if(mfxy[i][0]==1){
				return mfxy[i][1]*height+mfxy[i][2];
			}
		}
		return -1;
	}

	//该对方下，但是我有活三的情况
	private boolean scanWinThree(int colour,int[][] omap,int txy[][],int index){
		//scan three
		int value;
		int three=0;
		int cmap[][]=new int[length][height];
		for(int i=0;i<4;i++){
			if(txy[i][0]==1){
				if(omap[txy[i][1]][txy[i][2]]!=0)continue;
				value=scanMap(colour==1?2:1,omap,txy[i][1],txy[i][2],false);
				if(value>=valueWin)return false;
				if(value>valueMin){
					mapCopy(omap,cmap);
					cmap[txy[i][1]][txy[i][2]]=colour==1?2:1;
					if(scanWin(colour,cmap,index+1,stopFour(),txy)<0)return false;
				}
				three++;
			}else if(txy[i][0]==2){
				if(omap[txy[i][3]][txy[i][4]]!=0)continue;
				value=scanMap(colour==1?2:1,omap,txy[i][3],txy[i][4],false);
				if(value>=valueWin)return false;
				if(value>valueMin){
					mapCopy(omap,cmap);
					cmap[txy[i][3]][txy[i][4]]=colour==1?2:1;
					if(scanWin(colour,cmap,index+1,stopFour(),txy)<0)return false;
				}
				three++;
			}else if(txy[i][0]==3){
				if(omap[txy[i][1]][txy[i][2]]!=0 || omap[txy[i][3]][txy[i][4]]!=0)continue;
				value=scanMap(colour==1?2:1,omap,txy[i][1],txy[i][2],false);
				if(value>=valueWin)return false;
				if(value>valueMin){
					mapCopy(omap,cmap);
					cmap[txy[i][1]][txy[i][2]]=colour==1?2:1;
					if(scanWin(colour,cmap,index+1,stopFour(),txy)<0)return false;
				}
				value=scanMap(colour==1?2:1,omap,txy[i][3],txy[i][4],false);
				if(value>=valueWin)return false;
				if(value>valueMin){
					mapCopy(omap,cmap);
					cmap[txy[i][3]][txy[i][4]]=colour==1?2:1;
					if(scanWin(colour,cmap,index+1,stopFour(),txy)<0)return false;
				}
				three++;
			}
		}
		if(three==0)return false;
		//scan other
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(omap[i][j]!=0)continue;
			if(checkEmpty(omap,i,j))continue;
			value=scanMap(colour==1?2:1,omap,i,j,false);
			if(value>=valueWin)return false;
			if(value<54)continue;
			value=stopFour();
			if(value>=0){
				mapCopy(omap,cmap);
				cmap[i][j]=colour==1?2:1;
				if(scanWin(colour,cmap,(three>1?index:index+1),value,txy)<0)return false;
			}
		}
		return true;
	}

	private boolean scanWinS(int colour,int[][] omap,int x,int y,int index){
		//pre scan
		int value=scanMap(colour,omap,x,y,false);
		if(value==valueMin)return false;
		if(value>=valueWin)return true;
		if(value<53)return false;
		//deep scan
		int cmap[][]=new int[length][height];
		mapCopy(omap,cmap);
		cmap[x][y]=colour;
		int txy[][]=new int[4][5];
		for(int i=0;i<4;i++)
		for(int j=0;j<5;j++)
			txy[i][j]=mtxy[i][j];
		//scan four
		value=stopFour();
		if(value>=0){
			if(scanMap(colour==1?2:1,cmap,value/height,value%height,false)>=valueWin)return false;
			cmap[value/height][value%height]=colour==1?2:1;
			if(scanWin(colour,cmap,index+1,stopFour(),txy)>=0)return true;
			return false;
		}
		//scan three
		return scanWinThree(colour,cmap,txy,index);
	}

	private int[][] findThree(int colour,int[][] omap){
		int txy[][]=new int[4][5];
		int tindex=0;
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(omap[i][j]!=0)continue;
			if(checkEmpty(omap,i,j))continue;
			if(scanMap(colour,omap,i,j,false)>=valueWin){
				if(tindex>=4)break;
				if(txy[tindex][0]==0){
					txy[tindex][0]=1;
					txy[tindex][1]=i;
					txy[tindex][2]=j;
				}else{
					txy[tindex][0]=3;
					txy[tindex][3]=i;
					txy[tindex][4]=j;
					tindex++;
				}
			}
		}
		return txy;
	}

	private int scanWin(int colour,int[][] omap,int index,int ij,int txy[][]){
		if(index>mdeep)return -1;
		sdetail[index]=0;
		//pre scan
		if(ij>=0){
			sdetail[index]=9;
			if(scanWinS(colour,omap,ij/height,ij%height,index))return ij;
			int cmap[][]=new int[length][height];
			mapCopy(omap,cmap);
			cmap[ij/height][ij%height]=colour;
			if(scanWinThree(colour,cmap,txy,index))return ij;
			return -1;
		}
		//first scan
		int total=0;
		int scaned=0;
		int values[][]=new int[length][height];
		for(int o=0;o<(length*height);o++){
			int i=order[o][0];
			int j=order[o][1];
			if(omap[i][j]!=0)continue;
			if(checkEmpty(omap,i,j))continue;
			int value=scanMap(colour,omap,i,j,false);
			if(value==valueMin)continue;
			if(value>=valueWin)return i*height+j;
			if(value<53)continue;
			if(value>100){
				values[i][j]=3;
			}else if(stopFour()>=0){
				values[i][j]=2;
			}else{
				values[i][j]=1;
			}
			total++;
		}
		//deep scan
		for(int k=3;k>0;k--){
			for(int o=0;o<(length*height);o++){
				int i=order[o][0];
				int j=order[o][1];
				if(values[i][j]!=k)continue;
				scaned++;
				sdetail[index]=(scaned*10-1)/total;
				if(scanWinS(colour,omap,i,j,index))return i*height+j;
			}
		}
		return -1;
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

	public boolean checkHandcut(int colour,int x,int y){
		if(x>=length || x<0 || y>=height || y<0)return true;
		if(colour!=1 && colour!=2)return true;
		if(colour==2 || !handcut)return false;
		return scanMap(1,mmap,x,y,true)==valueMin;
	}

	public static void main(String args[])throws Exception{
		Five five=new Five();
		five.put(5,5,1);
		five.put(4,6,1);
		five.put(6,6,1);
		five.put(5,7,1);
		//five.put(5,6,1);
		//five.put(6,7,1);
		five.setHandcut(true);
		//five.changeDeep(0);
		if(!five.getPoint(2))return;
		System.out.println(five.readX());
		System.out.println(five.readY());
		System.out.println(five.checkHandcut(1,5,6));
	}
}
