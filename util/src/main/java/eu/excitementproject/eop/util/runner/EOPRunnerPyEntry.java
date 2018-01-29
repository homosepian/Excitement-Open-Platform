package eu.excitementproject.eop.util.runner;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ImplCommonConfig;
import eu.excitementproject.eop.common.utilities.file.FileUtils;
import eu.excitementproject.eop.core.ClassificationTEDecision;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.OpenNLPTaggerEN;
import org.apache.uima.jcas.JCas;
import py4j.GatewayServer;

import java.io.File;
import java.io.IOException;

/**
 * Created by nik on 30/11/17.
 */
public class EOPRunnerPyEntry {
    private EOPRunner eopRunner;

    public EOPRunnerPyEntry(String[] args) {
        /**
        setDefaults();

        String config = "default_path";
        if(args.length == 1){
            config = getParams();
        }
         */
    }

    public void startAnalysis(){
        preprocess();
        createNewModels();
        annotateSingleT_H_Pair();
    }

    public void preprocess(){
        String dataSet =
                "data-set/English_dev.xml";
        String dirOut = "/tmp/";
        LAPAccess ttLap = null;
        try {
            //ttLap = new TreeTaggerEN(); //this requires to have TreeTagger already installed
            ttLap = new OpenNLPTaggerEN(); //the OpenNLP tagger
        } catch (LAPException e) {
            System.err.println("Unable to initiated LAP: " + e.getMessage());
            System.exit(1);
        }

        try {
            //the English RTE data set
            File f = FileUtils.loadResource(dataSet);
            //the output directory; the directory has to exist prior to starting.
            File outputDir = new File(dirOut);
            ttLap.processRawInputFormat(f, outputDir);
        } catch (LAPException | IOException e) {
            System.err.println("Failed to process EOP RTE data format: " + e.getMessage());
            System.exit(1);
        }

    }
    public void createNewModels(){
        String configurationFile = "configuration-file/MaxEntClassificationEDA_Base+OpenNLP_EN.xml";

        CommonConfig config = null;
        try {
            //This is the configuration file to be used with the selected EDA (i.e. MaxEntClassification EDA).
            File configFile = FileUtils.loadResource(configurationFile);

            config = new ImplCommonConfig(configFile);
        }
        catch (ConfigurationException | IOException e) {
            System.err.println("Failed to read configuration file: "+ e.getMessage());
            System.exit(1);
        }

        try {
            @SuppressWarnings("rawtypes")
            EDABasic eda = null;
            //creating an instance of MaxEntClassification EDA
            eda = new MaxEntClassificationEDA();
            //EDA initialization and start training
            eda.startTraining(config); // This *MAY* take some time.
            eda.shutdown(); //shutdown
        } catch (EDAException e) {
            System.err.println("Failed to do the training due to EDA Exception: "+ e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (ConfigurationException e) {
            System.err.println("Failed to do the training due to a config Exception: "+ e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (ComponentException e) {
            System.err.println("Failed to do the training due to a component Exception: "+ e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void annotateSingleT_H_Pair(){
        String configurationFile = "configuration-file/MaxEntClassificationEDA_Base+OpenNLP_EN.xml";
        String T = "The students had 15 hours of lectures and practice sessions on the topic of Textual Entailment.";
        String H = "The students must have learned quite a lot about Textual Entailment.";

        //1) Pre-processing T/H pair by using the LAP (i.e. OpenNLP)
        JCas annotated_THpair = null;
        try {
            LAPAccess lap = new OpenNLPTaggerEN(); // make a new OpenNLP based LAP
            annotated_THpair = lap.generateSingleTHPairCAS(T, H); // ask it to process this T-H.
        } catch (LAPException e) {
            System.err.print(e.getMessage());
            System.exit(1);
        }
        //
        //2) Initialize an EDA with a configuration (& corresponding model). You have to check that the
        //   model path in the configuration file points to the directory where the model is, e.g.:
        //   home/user_name/eop-resources-1.2.3/model/MaxEntClassificationEDAModel_Base+OpenNLP_EN
        //
        System.out.println("Initializing the EDA.");
        EDABasic<ClassificationTEDecision> eda = null;
        try {
            // TIE (i.e. MaxEntClassificationEDA): a simple configuration with no knowledge resource.
            File configFile = FileUtils.loadResource(configurationFile);
            CommonConfig config = new ImplCommonConfig(configFile);
            eda = new MaxEntClassificationEDA();
            eda.initialize(config);
        } catch (Exception e) {
            System.err.print(e.getMessage());
            System.exit(1);
        }
        //
        //
        // 3) Now, one input data is ready, and the EDA is also ready.
        //    Call the EDA.
        //
        System.out.println("Calling the EDA for decision.");
        TEDecision decision = null; // the generic type that holds Entailment decision result
        try {
            decision = eda.process(annotated_THpair);
        } catch (Exception e) {
            System.err.print(e.getMessage());
            System.exit(1);
        }
        //
        System.out.println("Run complete: EDA returned decision: " + decision.getDecision().toString());
        //
    }

    public static void main(String[] args) {
        EOPRunnerPyEntry eopRunnerPyEntry = new EOPRunnerPyEntry(args);
        GatewayServer gatewayServer = new GatewayServer(eopRunnerPyEntry);
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }
}
