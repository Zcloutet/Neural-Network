import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.lang.Math.*;
import javax.print.attribute.Size2DSyntax;
import java.util.*;
import java.io.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Network {
    public static void main(String[] args)throws IOException {
        //arraylists for both data sets
        ArrayList<List<Double>> set = new ArrayList<List<Double>>();
        ArrayList<List<Double>> test_set = new ArrayList<List<Double>>();
        
        //my path for csv files
        File file = new File("mnist_train.csv");
        File file1 = new File("mnist_test.csv");
        import_csv(set,file);
        import_csv(test_set,file1);

        //randomize data sets
        Collections.shuffle(set);
        Collections.shuffle(test_set);

        //index keys of neurons. all important values are stored in each neuron
        //left hand weight means weights from previous layer pointing at each neuron 
        /*mid neuron
        0:activations = 1
        1:left hand weight = 784
        2:bias = 1
        3:weight grad = 784 
        4:bias grad = 1 
        5:sum of weight grad for mini batch
        6:sum of bias grad for mini batch
        */
        ArrayList<List<List<Double>>> mid_neuron = new ArrayList<>(30);
        create_neuron(mid_neuron, 30, 784, 1);
        /*final neuron
        0:activations = 1
        1:left hand weight = 30
        2:bias = 1
        3:weight grad = 30
        4:bias grad = 1 
        5:sum of weight grad for mini batch
        6:sum of bias grad for mini batch
        */
        ArrayList<List<List<Double>>> fin_neuron = new ArrayList<>(10);
        create_neuron(fin_neuron, 10, 30, 1);

        boolean curr = false; 
        boolean run = false;
        //run program loop
        //takes input 1,2,3,4,5,0
        do{
            Scanner in = new Scanner(System.in);
            int inp=-1;
            try{
                System.out.println("[1] Train the network");
                System.out.println("[2] Load a pretrained network");
                System.out.println("[3] Run current network on training data");
                System.out.println("[4] Run current network on testing data");
                System.out.println("[5] Save the current weight set to file");
                System.out.println("[0] Exit");
                System.out.println("");
                inp = in.nextInt();
            
            }catch(InputMismatchException e){
                System.out.println("Invalid input");
                in.nextLine();
            }
            if(inp == 1){
                curr = true;
                train(set,mid_neuron,fin_neuron);

            }
            else if(inp == 2){

                System.out.print("Input weight file load location: ");
                //C:/Users/zclou/Desktop/CSC 475/NN/weight.txt
                load(mid_neuron,fin_neuron);
                System.out.println(" ");
                curr = true; 
            }
            else if(inp ==3 && curr){
                run_train(set, mid_neuron, fin_neuron);
            }
            else if(inp ==4 && curr){
                run_test(set, mid_neuron, fin_neuron);
            }
            else if(inp == 5 && curr){
                System.out.print("Input weight file save location: ");
                save(mid_neuron,fin_neuron);
            }
            else if(inp ==0){
                System.exit(0); 
            }
            else if( curr == false){
                    System.out.println("No valid weight set in network");
            }
        }while(run ==false);          
    }
    //save weight set to file
    static void save(ArrayList<List<List<Double>>>mid_neuron,ArrayList<List<List<Double>>>fin_neuron){    
        Scanner in = new Scanner(System.in);    
        try{
            String token = in.nextLine();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(token)));
           
            for (int i=0; i<10; i++){
                for(int j=0; j<30; j++){
                    out.println(fin_neuron.get(i).get(1).get(j));
                }
            }
            for (int k=0; k<30; k++){
                for(int j=0; j<784; j++){
                    out.println(mid_neuron.get(k).get(1).get(j));
                }
            } 
            out.close();
        }catch (IOException e) {
            System.out.println("Specified path not found");    
            return;
        }       
    }
    //load weight set from file
    static void load(ArrayList<List<List<Double>>>mid_neuron,ArrayList<List<List<Double>>>fin_neuron){
        Scanner in = new Scanner(System.in);
        try{    
            
            String token = in.nextLine();
            Scanner scan = new Scanner(new File(token)); 
            while(scan.hasNext())
            {
                
                for (int i=0; i<10; i++){
                    for(int j=0; j<30; j++){
                        fin_neuron.get(i).get(1).set(j,Double.parseDouble(scan.next()));
                    }
                }
                for (int k=0; k<30; k++){
                    for(int j=0; j<784; j++){
                        mid_neuron.get(k).get(1).set(j,Double.parseDouble(scan.next()));
                    }
                }           
            }
        }catch (FileNotFoundException e) {
            System.out.println("Not valid file, weights will be randomized.");
            return;
        }
        
    }
    //classified_digits = correct classifications
    //correct_digits = total occurences of digit
    static void print_stats(int[] classified_digits,int[] correct_digits,int m){
        
        System.out.println("Epoch "+(m+1));
        System.out.print("0: "+classified_digits[0]+"/"+correct_digits[0]+" ");
        System.out.print("1: "+classified_digits[1]+"/"+correct_digits[1]+" ");
        System.out.print("2: "+classified_digits[2]+"/"+correct_digits[2]+" ");
        System.out.print("3: "+classified_digits[3]+"/"+correct_digits[3]+" ");
        System.out.print("4: "+classified_digits[4]+"/"+correct_digits[4]+" ");
        System.out.println("5: "+classified_digits[5]+"/"+correct_digits[5]+" ");
        System.out.print("6: "+classified_digits[6]+"/"+correct_digits[6]+" ");
        System.out.print("7: "+classified_digits[7]+"/"+correct_digits[7]+" ");
        System.out.print("8: "+classified_digits[8]+"/"+correct_digits[8]+" ");
        System.out.print("9: "+classified_digits[9]+"/"+correct_digits[9]+" ");
        int [] total = new int [2];
        for(int g=0; g<10; g++){
            total[0] += classified_digits[g];
            total[1] += correct_digits[g];
        }
        double acc = ((double)total[0]/(double)total[1])*100;
        System.out.println("Accuracy: "+total[0]+"/"+total[1]+" = "+acc+"%");
        System.out.println("------------------------------------------------");
    }
    //runs current weight/bias set over training data 
    static void run_train(ArrayList<List<Double>> set, ArrayList<List<List<Double>>> mid_neuron,ArrayList<List<List<Double>>> fin_neuron){
        //training batch
        int x = 6000;
        int y = 10;
        ArrayList<List<List<Double>>> batch = new ArrayList<>(x);
        int count = 0;
        for(int i=0;i<x;i++)
        {
            batch.add(new ArrayList<List<Double>>(y));
           for (int j=0;j<10;j++) 
           {
                batch.get(i).add((set.get(count)));
                count++;
           }
        }       
        System.out.println("Running on Training Set");
       //feed forward training set
       for(int m=0; m<1; m++){
           int[] correct_digits = new int []{0,0,0,0,0,0,0,0,0,0};
           int[] classified_digits = new int []{0,0,0,0,0,0,0,0,0,0};
           for(int j=0; j<6000; j++){

               for(int i=0; i<10;i++)
               {
                   //compute activations for input
                   feedfrd(batch.get(j).get(i),mid_neuron,fin_neuron);
                   //change the output to 10x1 vector
                   //back propogate
                   //backprp(batch.get(j).get(i),mid_neuron,fin_neuron);
                   
                   stats(batch.get(j).get(i),fin_neuron,classified_digits,correct_digits);
               
               }
               //update here
               //update(mid_neuron, fin_neuron);
           }
           print_stats(classified_digits, correct_digits, m);
       }    


    }
    //runs current weight/bias set over testing data 
    static void run_test(ArrayList<List<Double>> set, ArrayList<List<List<Double>>> mid_neuron,ArrayList<List<List<Double>>> fin_neuron){

        //populate arraylist batch
        int x = 1000;
        int y =10;
        ArrayList<List<List<Double>>> batch = new ArrayList<>(x);
        int count = 0;
        for(int i=0;i<x;i++)
        {
            batch.add(new ArrayList<List<Double>>(y));
           for (int j=0;j<10;j++) 
           {
                batch.get(i).add((set.get(count)));
                count++;
           }
        }
        //run feed forward 1 epochs
       System.out.println("Running on Test Set");
       for(int m=0; m<1; m++){
        int[] correct_digits = new int []{0,0,0,0,0,0,0,0,0,0};
        int[] classified_digits = new int []{0,0,0,0,0,0,0,0,0,0};
        for(int j=0; j<1000; j++){

            for(int i=0; i<10;i++)
            {
                //compute activations for input
                feedfrd(batch.get(j).get(i),mid_neuron,fin_neuron);
   
                stats(batch.get(j).get(i),fin_neuron,classified_digits,correct_digits);
            
            }
        }
        print_stats(classified_digits, correct_digits, m);
    }

    }
    //Train the network with randomized weights and biases
    static void train(ArrayList<List<Double>> set, ArrayList<List<List<Double>>> mid_neuron,ArrayList<List<List<Double>>> fin_neuron){
        //training batch
        int x = 6000;
        int y = 10;
        ArrayList<List<List<Double>>> batch = new ArrayList<>(x);
        int count = 0;
        for(int i=0;i<x;i++)
        {
            batch.add(new ArrayList<List<Double>>(y));
           for (int j=0;j<10;j++) 
           {
                batch.get(i).add((set.get(count)));
                count++;
           }
        }       
        System.out.println("Training the Network");

       //run SGD 30 epochs
       for(int m=0; m<30; m++){
           int[] correct_digits = new int []{0,0,0,0,0,0,0,0,0,0};
           int[] classified_digits = new int []{0,0,0,0,0,0,0,0,0,0};
           for(int j=0; j<6000; j++){

               for(int i=0; i<10;i++)
               {
                   //compute activations for input
                   feedfrd(batch.get(j).get(i),mid_neuron,fin_neuron);
                   //change the output to 10x1 vector
                   
                   //back propogate
                   backprp(batch.get(j).get(i),mid_neuron,fin_neuron);
                   
                   stats(batch.get(j).get(i),fin_neuron,classified_digits,correct_digits);
               
               }
               //update here
               update(mid_neuron, fin_neuron);
               Collections.shuffle(set);
           }
           print_stats(classified_digits, correct_digits, m);
       }
    }
    
    static void stats(List<Double> batch, ArrayList<List<List<Double>>> fin_neuron,int[] classified_digits, int[]correct_digits){
        double y = batch.get(0);
        double output =-1;
        int node =0;
        for(int i=0; i<10; i++){
            //looks for max value in output layer
            if(fin_neuron.get(i).get(0).get(0)>output){
                output = fin_neuron.get(i).get(0).get(0);
                node = i;                        
            }
            if(y == i){
                correct_digits[i]+=1;
            }
        }
        if (node == y){
            classified_digits[node]+=1;
        }
    }

    static void update(ArrayList<List<List<Double>>> mid_neuron,ArrayList<List<List<Double>>> fin_neuron){    
        //learning rate and mini batch size
        double rate = 3;
        double size = 10;
        // update weights and biases for middle layer
        for(int i=0; i<30; i++){
            double bias_old = mid_neuron.get(i).get(2).get(0);
           
            double bias_sum = mid_neuron.get(i).get(6).get(0);
            //forced to set sum variable to 0 after updating
            mid_neuron.get(i).get(6).set(0,0.0);
           
            double bias_new = bias_old-(rate/size)*bias_sum;
            mid_neuron.get(i).get(2).set(0,bias_new);
            for(int j=0; j<784; j++){
                double weight_old = mid_neuron.get(i).get(1).get(j);
                
                double weight_sum = mid_neuron.get(i).get(5).get(j);
                mid_neuron.get(i).get(5).set(j,0.0);
                
                double weight_new = weight_old - (rate/size)*weight_sum;
                mid_neuron.get(i).get(1).set(j,weight_new);
            }
        }
        // update w+b for final layer
        for(int i=0; i<10; i++){
            double bias_old = fin_neuron.get(i).get(2).get(0);
            
            double bias_sum = fin_neuron.get(i).get(6).get(0);
            fin_neuron.get(i).get(6).set(0,0.0);

            double bias_new = bias_old-(rate/size)*bias_sum;  
            fin_neuron.get(i).get(2).set(0,bias_new);
            for(int j=0; j<30; j++){
                double weight_old = fin_neuron.get(i).get(1).get(j);
               
                double weight_sum = fin_neuron.get(i).get(5).get(j);
                fin_neuron.get(i).get(5).set(j,0.0);

                double weight_new = weight_old - (rate/size)*weight_sum;
                fin_neuron.get(i).get(1).set(j,weight_new);
            }
        }
    }

    
    static void backprp(List<Double> batch,ArrayList<List<List<Double>>> mid_neuron,ArrayList<List<List<Double>>> fin_neuron){
        //first compute bias and weight gradients of final layer
        //move back at each level and compute error on each node
       
        //convert y to 10X1 vector
        double y[] = onehot(batch);
        
        //gradients of final layer
        for(int i=0; i<10;i++){
            double a;
            a = fin_neuron.get(i).get(0).get(0);
            double bias_grad = (a-y[i])*a*(1-a);

            //compute sum for all bias grad in final layer
            double bsum = fin_neuron.get(i).get(6).get(0);
            bsum = bsum+bias_grad;
            fin_neuron.get(i).get(6).set(0,bsum);

            //compute bias grad
            fin_neuron.get(i).get(4).set(0,bias_grad);
            //weight gradient
            for(int j=0; j<30; j++){
                //activation of each neuron in middle layer
                double a_mid = mid_neuron.get(j).get(0).get(0);  
                //weight grad of 300 weights connected mid and final
                double weight_grad = bias_grad*a_mid;

                //compute sum for all weight grad in this batch
                double wsum = fin_neuron.get(i).get(5).get(j);
                wsum = wsum+weight_grad;
                fin_neuron.get(i).get(5).set(j,wsum);

                fin_neuron.get(i).get(3).set(j,weight_grad);              
            }          
        }
        //gradients of middle layer
        for(int i=0; i<30; i++){
            double a = mid_neuron.get(i).get(0).get(0);
            double sum=0;
            for(int j=0; j<10; j++){
                double fin_bias_grad = fin_neuron.get(j).get(4).get(0);
                double weight = fin_neuron.get(j).get(1).get(i);
                sum += fin_bias_grad*weight;
            }
            //bias gradient for middle
            double mid_bias_grad = sum*a*(1-a);
            
            double bsum = mid_neuron.get(i).get(6).get(0);
            bsum = bsum+mid_bias_grad;
            mid_neuron.get(i).get(6).set(0,bsum);

            mid_neuron.get(i).get(4).set(0,mid_bias_grad);
            //weight gradient for each weight connected to node i
            for(int k=0; k<784; k++){
                double weight_grad = mid_bias_grad*batch.get(k+1);
                //weight grad of weights connected input and mid
                double wsum = mid_neuron.get(i).get(5).get(k);
                wsum = wsum+weight_grad;
                mid_neuron.get(i).get(5).set(k,wsum);
                mid_neuron.get(i).get(3).set(k,weight_grad);
            }
        }               
    }

    static void feedfrd(List<Double> batch,ArrayList<List<List<Double>>> mid_neuron,ArrayList<List<List<Double>>> fin_neuron){
        //iterate through neurons in middle layer
        for(int i=0; i<30; i++){
            double sum=0;
            double bias = mid_neuron.get(i).get(2).get(0);
            for (int j=0; j<784; j++){
                //sum up weight*input
                double weight = mid_neuron.get(i).get(1).get(j);
                sum += weight*batch.get(j+1);       
            }
            sum+=bias;
             //compute activation 
            mid_neuron.get(i).get(0).set(0,sigmoid(sum));
        }
        //iterate through neurons in final layer
        for (int i=0; i<10; i++){
            double sum=0;
            double bias = fin_neuron.get(i).get(2).get(0);
            for (int j=0; j<30; j++){
                double weight = fin_neuron.get(i).get(1).get(j);
                sum += weight*mid_neuron.get(j).get(0).get(0);
            }
            sum+= bias;
            //compute activation
            fin_neuron.get(i).get(0).set(0,sigmoid(sum));
        }
    }

    static void import_csv(ArrayList<List<Double>>set,File file){
        Scanner inputStream;
        try{
            inputStream = new Scanner(file);

            while(inputStream.hasNext()){
                String line= inputStream.next();
                String[] values = line.split(",");
                Double[] stuff = new Double [785];
                for(int j=0;j<785;j++){
                    if (j!=0){
                        double temp = Double.parseDouble(values[j]);
                        stuff[j] = temp/255;
                    }
                    //separate y value so it doesnt get normalized
                    else{
                            stuff[j] = Double.parseDouble(values[j]);
                            
                        }
                }
                //adds the currently parsed line to the array
                set.add(Arrays.asList(stuff));
            }
            inputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    } 

    static double sigmoid(double z){
        return 1/(1+Math.exp(-z));
    }

    static double sigmoid_prime(double z){
        return sigmoid(z)*(1-sigmoid(z));
    }

    static void create_neuron(ArrayList<List<List<Double>>> neuron,int size,int weight, int bias){
        // populate neurons
        // have to fill arraylist with loose values in order for later to work
        // also randomize weights and biases
        Random r = new Random();
        for(int i=0;i<size;i++)
        {
            neuron.add(new ArrayList<List<Double>>(7));
            neuron.get(i).add(new ArrayList<Double>(1));
            neuron.get(i).get(0).add(0.0);
            neuron.get(i).add(new ArrayList<Double>(weight));
            neuron.get(i).add(new ArrayList<Double>(bias));
            neuron.get(i).add(new ArrayList<Double>(weight));
            neuron.get(i).add(new ArrayList<Double>(bias));

            //sums
            neuron.get(i).add(new ArrayList<Double>(weight));
            neuron.get(i).add(new ArrayList<Double>(size));
            neuron.get(i).get(6).add(0.0);  

            // randomize weights and biases
            for (int k=0; k<weight;k++){
                neuron.get(i).get(1).add(r.nextGaussian());
                neuron.get(i).get(3).add(0.0);
                neuron.get(i).get(5).add(0.0);
            }
            for (int k=0; k<bias;k++){
                neuron.get(i).get(2).add(r.nextGaussian());
                neuron.get(i).get(4).add(0.0);
            }         
        }
    }
    //converts y value toonehot vector 
    static double[] onehot(List<Double> batch){
        double vec [] = new double[] {0,0,0,0,0,0,0,0,0,0};
        double temp = batch.get(0);
        if (temp == 0)
        {
            vec[0] = 1;
        }
        if (temp == 1)
        {
            vec[1] = 1;
        }
        if (temp== 2)
        {
            vec[2] = 1;
        }
        if (temp == 3)
        {
            vec[3] = 1;
        }
        if (temp == 4)
        {
            vec[4] = 1;
        }
        if (temp == 5)
        {
            vec[5] = 1;
        }
        if (temp == 6)
        {
            vec[6] = 1;
        }
        if (temp == 7)
        {
            vec[7] = 1;
        }
        if (temp == 8)
        {
            vec[8] = 1;
        }
        if (temp == 9)
        {
            vec[9] = 1;
        }
        return vec;
    }

}
