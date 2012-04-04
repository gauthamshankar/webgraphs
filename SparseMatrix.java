package heatdiffusion;

import java.util.ArrayList;
import cern.colt.*;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

public class SparseMatrix {
    
    protected ArrayList val;
    protected ArrayList colInd;
    protected ArrayList rowPtr;
    
    protected int numRows;
    protected int numColumns;
        
    int[][] sparse = {
        {10,0,0,12,0},
        {0,0,11,0,13},
        {0,16,0,0,0},
        {0,0,11,0,13}
    }; 
        
    public SparseMatrix(ArrayList v, ArrayList c, ArrayList r, int row, int column)
    {
        val = new ArrayList();
        val = v;
        colInd = new ArrayList();
        colInd = c;
        rowPtr = new ArrayList();
        rowPtr = r;
        numRows = row;
        numColumns = column;
    }
    
    public void testColt()
    {
        SparseDoubleMatrix2D t = new SparseDoubleMatrix2D(1000, 1000) ;
       // t.s
    }
    
    public void testCreate()
    {
        int counter = -1;
        int flag = 0;
        
        for(int i =0; i<sparse.length ;i++) 
        {
            flag = 0;
            for(int j=0; j<sparse[i].length; j++) 
            {
                int value = sparse[i][j];
                if(value != 0) {
                    /* Counter for row pointer */
                    ++counter;
                    
                    val.add(new Integer(value));
                    colInd.add(new Integer(j));
                    
                    if(flag == 0) {
                        rowPtr.add(new Integer(counter));
                        flag = 1;
                    }
                }
            }
        }
        rowPtr.add(new Integer(counter+1));
    }
    
    public void display()
    {
        int check =0;
        int f = 0;
        
        /* display */
        for(int i =0 ; i<numRows; i++)
        {
            int start = (Integer) rowPtr.get(i);
            int end = (Integer) rowPtr.get(i+1);
            int sub = end - start;

            for(int j=0; j<numColumns; j++)
            {
                float value = (float) 0.0;
                for(int k = start; k< end; k++)
                    {
                        int t = (Integer)colInd.get(k);
                        if(j == t)
                        {
                            ++check;
                            value =(Float)val.get(k);
                            f=1;
                            break;
                        }
                    }
            
                if(f == 1) {
                    System.out.print(value+"\t");
                    f=0;
                }
                else
                    System.out.print(0+"\t");
            }
            System.out.print("\n");
            if(check != sub)
                System.out.println("Error : "+check+" "+sub);
            check =0;
        }
    }
    
    public void update(int i, int j, float v)
    {
        int changei = i;
        int changej = j;
        float changeVal = v;
        
        if(changeVal == 0.0)
            return;
        
        int ptr = (Integer) rowPtr.get((changei));
        int nextPtr = (Integer) rowPtr.get(changei+1);
        
        for(int k=ptr; k<nextPtr; k++)
        {
            int t = (Integer)colInd.get(k);
            /* If element is present */
            if(changej == t )
            {
                val.set(k, changeVal);
                return;
            }
            else
            /* If element is not present, 
             * Will handel adding to beginning of colInd for that row
             * And chaning the rowPtr 
             */
            if(t > changej)
            {
                colInd.add(k, changej);
                val.add(k, changeVal);
                for(int l=changei+1; l<rowPtr.size(); l++) {
                    int changePtr = (Integer) rowPtr.get(l);
                    rowPtr.set(l, changePtr+1);           
                }
     
                return;
            }
        }
        /* If element is not present, 
         * Will handel adding to end of colInd for that row
         * And changing the rowPtr
         */
        colInd.add(nextPtr,changej);
        val.add(nextPtr,changeVal);
        for(int l=changei+1; l<rowPtr.size(); l++) {
            int changePtr = (Integer) rowPtr.get(l);
            rowPtr.set(l, changePtr+1);           
        }
    }
    
    public boolean contains(int i, int j)
    {
        int isi = i;
        int isj = j;
        int t = 0;
        
        int ptr = (Integer) rowPtr.get((isi));
        int nextPtr = (Integer) rowPtr.get(isi+1);
        
        for(int k=ptr; k<nextPtr; k++)
        {
            t = (Integer)colInd.get(k);
            if(t > isj)
            {
                return false;
            }
            /* If element is present */
            if(isj == t )
            {
                return true;
            }
        }
        return false;
    }
    
    public int containsOrNext(int i, int j)
    {
        int isi = i;
        int isj = j;
        int t = 0;
        
        int ptr = (Integer) rowPtr.get((isi));
        int nextPtr = (Integer) rowPtr.get(isi+1);
        
        for(int k=ptr; k<nextPtr; k++)
        {
            t = (Integer)colInd.get(k);
            if(t > isj)
            {
                return t;
            }
            /* If element is present */
            if(isj == t )
            {
                return t;
            }
        }
        return t;
    }
    
    public float get(int i, int j)
    {
        int isi = i;
        int isj = j;
        
        int ptr = (Integer) rowPtr.get((isi));
        int nextPtr = (Integer) rowPtr.get(isi+1);
        
        for(int k=ptr; k<nextPtr; k++)
        {
            int t = (Integer)colInd.get(k);
            /* If element is present */
            if(isj == t )
            {
                float v = (Float)val.get(k);
                return v ;
            }
        }
        return -100;
    }
    
    public int getRow()
    {
        return numRows;
    }
    
    public int getColumn()
    {
        return numColumns;
    }
    
    public SparseMatrix mul(SparseMatrix H)
    {
        ArrayList AN = new ArrayList();
        ArrayList AJ = new ArrayList();
        ArrayList AI = new ArrayList();
        
        float temp = 0;
        int counter = -1,flag =0;
        
        for (int i = 0; i < H.getColumn() ; i++) 
        {
            System.out.println(i);
            flag = 0;
            for (int j = 0; j < H.getColumn(); j++)
            {
                int b=0;
                b = H.containsOrNext(i,0);
                for (int k = 0; k < H.getColumn(); k++)
                {
                    //b = H.containsOrNext(i,k);
                    if(b == k)
                    {
                        b = H.containsOrNext(i,k+1);
                    }
                    else
                    if(k > b)
                    {
                        b = H.containsOrNext(i,k);
                        if(b == k)
                        {
                            
                        }
                    }
                   // boolean c = H.contains(k, j);
//                    if(b == true && c == true)
//                    {
//                        float val1 = H.get(i, k);
//                        float val2 = H.get(k, j);
//                        temp += val1 * val2;
//                    }
                }
//                if(temp > 0)
//                {
//                    ++counter;
//                    AN.add(new Float(temp));
//                    AN.add(new Integer(j));
//                    if(flag == 0) {
//                        AI.add(new Integer(counter));
//                        flag = 1;
//                    }
//                }
                temp = 0;
            }
            
        }
        
        SparseMatrix c = new SparseMatrix(AN,AJ,AI,AI.size()-1,AI.size()-1);
        return c;
    }
    public void displayArray()
    {
        System.out.println("Value Array :");
        for(int i=0; i< val.size(); i++)
        {
            System.out.print(val.get(i)+" ");
        }
        System.out.println("\nSize : "+val.size()+"\n");
        
        System.out.println("Column Array :");
        for(int i=0; i< colInd.size(); i++)
        {
            System.out.print(colInd.get(i)+" ");
        }
        System.out.println("\nSize : "+colInd.size()+"\n");
        
        System.out.println("Row Array :");
        for(int i=0; i< rowPtr.size(); i++)
        {
            System.out.print(rowPtr.get(i)+" ");
        }
        System.out.println("\nSize : "+rowPtr.size()+"\n");
    }
        
        
}