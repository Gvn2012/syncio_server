import json

topics = [
    ("__consumer_offsets", 50),
    ("email-verification-v2", 2),
    ("image.uploaded", 2),
    ("org.created", 2),
    ("post-events-v2", 2),
    ("relationship-events-v2", 2)
]

partitions = []
for topic_name, count in topics:
    for i in range(count):
        partitions.append({
            "topic": topic_name,
            "partition": i,
            "replicas": [1]
        })

reassignment = {
    "version": 1,
    "partitions": partitions
}


with open('/Users/cps/IdeaProjects/syncio_server/scratch/reassignment.json', 'w') as f:
    json.dump(reassignment, f, indent=4)
