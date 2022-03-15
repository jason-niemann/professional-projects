import java.util.HashMap;

//This is the local cache. It just stores the first answer for any question in the cache (a response for
// google.com might return 10 IP addresses, it just stores the first one). This class has methods for
// querying and inserting records into the cache. If an entry is too old (its TTL has expired),
// it is removed.
public class DNSCache {

    //Store the DNS cache here
    static HashMap<DNSQuestion, DNSRecord> hashMap = new HashMap<>();

    //Search hashmap for key
    static boolean isInCache(DNSQuestion dnsQuestion) {

        //If the question is found
        if (hashMap.containsKey(dnsQuestion)) {

            //Check the timestamp
            if (hashMap.get(dnsQuestion).timestampValid()) {

                //If valid
                return true;
            } else {

                //If invalid, remove the record, return false
                hashMap.remove(dnsQuestion);
                return false;
            }
        } else {

            //If not found
            return false;
        }

    }

    //Setter for the hashmap
    static boolean addRecord(DNSQuestion dnsQuestion, DNSRecord dnsRecord) {
        //Add record
        hashMap.put(dnsQuestion, dnsRecord);

        //Confirm record was added
        return hashMap.get(dnsQuestion) == dnsRecord;
    }

    //Getter for DNSRecord
    static DNSRecord getRecord(DNSQuestion dnsQuestion) {
        return hashMap.get(dnsQuestion);
    }

}
