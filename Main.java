import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {
	
	static List<String> terms = new ArrayList<String>();
	static List<String> frequencies = new ArrayList<String>();
	static List<String> hitsNoContext = new ArrayList<String>();
	static List<String> hitsInContext = new ArrayList<String>();
	static List<MinedTerm> minedTerms = new ArrayList<MinedTerm>();
	
	//static int valText=1;
	private static final String FILE_PATH = "C:/Users/lsjeu/Desktop/texto1.xlsx";
	
	public Main() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws ParseException, IOException {
		String mainText;
		mainText = readFile("text3.txt");  // Coloca em mainText o texto que será minerado
		
		String context;
		
		try {
			context = sendPost(mainText);
			getAllHits(context);
			setRelation();
			writeXLS(minedTerms, context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Método que faz o POST no webservice e obtém os termos mais relevantes
	private static String sendPost(String text) throws Exception {
		String conceptsExtracted;
		List<String> conceptsVector = new ArrayList<String>();
		List<String> finalConcepts = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		URL url = new URL("http://gtech-srv01.nuvem.ufrgs.br/Lourenco/Sobek.php"); //Acessa o webservice do Sobek para minerar o texto
		Map<String,Object> params = new LinkedHashMap();

		params.put("entrada", "data="+text);

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,Object> param : params.entrySet()) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		} 

		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);

		//Para ler a resposta do servidor, com os termos e frequência
		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		for (int c; (c = in.read()) >= 0;){
			sb.append((char)c);
		}
		conceptsExtracted = sb.toString();
		String[] splitted = conceptsExtracted.split("\\,");
		for (String temp: splitted){ // Divide os termos separados por uma vírgula, entre o termo e o nº de ocorrências
			conceptsVector.add(temp);
		}
		for (int i=0; i<conceptsVector.size(); i++){
			String[] splittedAgain = conceptsVector.get(i).split("\\n");
			for (String temp: splittedAgain){ // Divide novamente, termos separados por "nova linha"
				finalConcepts.add(temp);
			}
		}
		for (int i = 0; i<finalConcepts.size(); i++){
			if(finalConcepts.get(i).contains(" ")){
				terms.add(("\""+finalConcepts.get(i)+"\"").toLowerCase()); // Se for um termo composto, coloca ele entre aspas
			}												
			else{
				terms.add((finalConcepts.get(i)).toLowerCase()); // Se não é composto, não coloca aspas
			}
			frequencies.add(finalConcepts.get(i+1)); // "Pula" o termo e pega apenas a frequência
			i++;
		}
		String context = "";
		context = context+terms.get(0)+" "+context+terms.get(1);
		terms.remove(terms.get(0));
		frequencies.remove(0);
		terms.remove(terms.get(0));
		frequencies.remove(0);
		return context;
	}
	
	static void getAllHits (String context) throws IOException{
		
		for (int i=0; i<terms.size(); i++){
			MinedTerm mt = new MinedTerm(); // Cria um novo termo, com hits s/ contexto, 1 contexto e 2 contextos, além da frequência
			
			hitsNoContext.add(searchGoogle(terms.get(i)));
			hitsInContext.add(searchGoogle(context+"+"+terms.get(i))); // Busca no Google o termo com 2 termos de contextos
		
			mt.setTerm(terms.get(i));
			mt.setHitsNoContext(hitsNoContext.get(i));
			mt.setContext(hitsInContext.get(i));
			mt.setFrequencyInText(frequencies.get(i));
			
			minedTerms.add(mt);
		}
	}
	
	// Busca no Google o que for passado em "String concepts"
	static String searchGoogle (String concepts) throws IOException{
		try{
			Thread.sleep((int)(2000 + Math.random() * 2001)); // Faz a Thread dormir entre 2 ou 4 segundos
		}catch (Exception e){};
		
		System.setProperty("http.proxyHost", "192.168.5.1");
		System.setProperty("http.proxyPort", "1080");
		
		String url = "https://www.google.com.br/search?hl=pt-br&tbs=li:1&q="+concepts;
		
		//String url = "https://www.google.com/search?as_epq="+concepts+"&lr=lang_pt&cr=countryBR&safe=images&hl=pt-br&tbs=li:1";
		String temp;
		Document document = Jsoup
		                   .connect(url)
		                   .timeout(30000)
		                   .userAgent("Mozilla/5.0 (Windows; U; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)")
		                   .get();

		Element divResultStats = document.select("div#resultStats").first();
		if (divResultStats==null) {
		    throw new RuntimeException("Unable to find results stats.");
		}
		temp = divResultStats.text().replaceAll("[^0-9]", "");
		return temp;
	}

	static void writeXLS (List<MinedTerm> mtList, String context){
		Workbook workbook = new XSSFWorkbook();
		Sheet termsSheet = workbook.createSheet("TermsInContext");
		
		String temp[];
		if(context.contains("\"")){
			temp = context.split("\"");
			temp[0] = temp[1];
			temp[1] = temp[2].replace(" ", "");
		}
		else{
			temp = context.split(" ");
		}
        
		int rowIndex = 1;
        Row row = termsSheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(temp[0]);
        row = termsSheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(temp[1]);
        
        row = termsSheet.createRow(0);
        int cellIndex = 0;
        row.createCell(cellIndex++).setCellValue("TERMO");
        row.createCell(cellIndex++).setCellValue("RESULTADOS SEM CONTEXTO");
        row.createCell(cellIndex++).setCellValue("RESULTADOS COM CONTEXTO");
        row.createCell(cellIndex++).setCellValue("FREQUÊNCIA NO TEXTO");
        row.createCell(cellIndex++).setCellValue("RELAÇÃO");
        
        for(MinedTerm term : mtList){
            row = termsSheet.createRow(rowIndex++);
  
            cellIndex = 0;
            row.createCell(cellIndex++).setCellValue(term.getTerm());
            row.createCell(cellIndex++).setCellValue(term.getHitsNoContext());
            row.createCell(cellIndex++).setCellValue(term.getContext());
            row.createCell(cellIndex++).setCellValue(term.getFrequencyInText());
            row.createCell(cellIndex++).setCellValue(term.getRelation());
        }
        
        try {
        	//valText++;
            FileOutputStream fos = new FileOutputStream(FILE_PATH);
            workbook.write(fos);
            fos.close();
            System.out.println(FILE_PATH + " is successfully written");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	static void setRelation (){
		for (int i=0; i<minedTerms.size(); i++){
			MinedTerm mt = minedTerms.get(i);
			int two = Integer.parseInt(mt.getContext());
			long noContext;
			noContext = Long.parseLong(mt.getHitsNoContext());
			double temp = (double)two/noContext;
			mt.setRelation(temp);
		}
	}
	
	// Método que lê o txt e converte em String
	static String readFile(String fileName) throws IOException { 
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
}
