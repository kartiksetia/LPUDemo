import copy
import json
import unittest
from unittest.mock import patch, MagicMock
import server

class QuizValidationTests(unittest.TestCase):
    def setUp(self):
        self.quiz = {'status':'ok','questions':[{'prompt':f'Question {i}', 'options':['A','B','C','D'], 'correctIndex':1,'explanation':'Grounded explanation'} for i in range(3)]}
    def test_valid_quiz(self):
        self.assertEqual(len(server.validate_quiz(self.quiz)['questions']),3)
    def test_rejects_bad_indexes_and_bool(self):
        for value in [-1,4,True]:
            with self.subTest(value=value):
                q=copy.deepcopy(self.quiz);q['questions'][0]['correctIndex']=value
                with self.assertRaises(ValueError):server.validate_quiz(q)
    def test_rejects_duplicate_and_missing_options(self):
        for options in [['A','A','B','C'],['A','B','C'],['','B','C','D']]:
            q=copy.deepcopy(self.quiz);q['questions'][0]['options']=options
            with self.assertRaises(ValueError):server.validate_quiz(q)
    def test_unclear_material(self):
        with self.assertRaises(ValueError):server.validate_quiz({'status':'needs_clearer_notes','questions':[]})
    def test_empty_input(self):
        with self.assertRaises(ValueError):server.generate({'notes':''})
    def test_provider_request_and_response(self):
        response=MagicMock();response.__enter__.return_value.read.return_value=json.dumps({'output_text':json.dumps(self.quiz)}).encode()
        with patch.dict('os.environ',{'GEMINI_API_KEY':'test-key'}), patch('server.urlopen',return_value=response) as call:
            self.assertEqual(len(server.generate({'notes':'Queue uses FIFO.'})['questions']),3)
            body=json.loads(call.call_args.args[0].data)
            self.assertFalse(body['store'])
            self.assertEqual(body['response_format']['mime_type'],'application/json')

if __name__=='__main__':unittest.main()
